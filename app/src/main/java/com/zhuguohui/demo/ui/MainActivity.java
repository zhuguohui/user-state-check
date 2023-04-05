package com.zhuguohui.demo.ui;

import static com.zhuguohui.demo.impl.DemoUserState.BindPhoneNumber;
import static com.zhuguohui.demo.impl.DemoUserState.BindRealName;
import static com.zhuguohui.demo.impl.DemoUserState.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.LayoutInflaterCompat;

import com.zhuguohui.demo.api.BaiduApi;
import com.zhuguohui.demo.api.BaiduHotListResult;
import com.zhuguohui.demo.api.HotItemBean;
import com.zhuguohui.demo.databinding.ActivityMainBinding;
import com.zhuguohui.demo.impl.DemoUserStateManager;
import com.zhuguohui.demo.toast.GZToast;
import com.zhuguohui.demo.userstate.CheckUserState;
import com.zhuguohui.demo.userstate.manager.UserStateManager;
import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;
import com.zhuguohui.demo.userstate.transform.UserStateTransform;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        binding.tvRest.setOnClickListener(v-> {
            DemoUserStateManager.getInstance().reset();
            GZToast.show("重置成功");
        });
        aopActivity();
        aopView();
        aopApi();

    }


    private void aopActivity() {
        binding.setActivity( UserStateManager.getProxy(compositeDisposable,this,this,e->{
            GZToast.error().show(e.getMessage());
        }));
    }

    private void aopView() {
        binding.layoutCollection.setOnClickListener(v->{
            GZToast.success().show("收藏成功");
        });
        binding.tvCollection.setOnClickListener(v -> {
            GZToast.success().show("收藏成功");
        });
    }


    private void aopApi() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://tenapi.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        BaiduApi baiduApi = retrofit.create(BaiduApi.class);

        BaiduApi finalBaiduApi = baiduApi;
        binding.btnGetBaiduHot.setOnClickListener(v->{
            Disposable disposable = finalBaiduApi.getHotList()
                    .compose(new UserStateTransform<>(this, Login|BindRealName))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(BaiduHotListResult::getData)
                    .subscribe(list -> {
                        StringBuffer buffer=new StringBuffer();
                        for(HotItemBean itemBean:list){
                            buffer.append(itemBean.getName()).append("\n");
                        }
                        binding.tvBaiduContent.setText(buffer);
                    }, e -> {
                        e.printStackTrace();
                        GZToast.error().show(e.getMessage());
                    });
            compositeDisposable.add(disposable);
        });
    }

    @CheckUserState(states = Login|BindPhoneNumber|BindRealName,policy = UserStateCheckPolicy.autoGo)
    public void doCollection(View view){
        GZToast.success().show("收藏成功");
    }
}