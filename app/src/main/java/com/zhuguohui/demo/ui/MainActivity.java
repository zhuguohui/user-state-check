package com.zhuguohui.demo.ui;

import static com.zhuguohui.demo.magnagerImpl.DemoUserState.BindPhoneNumber;
import static com.zhuguohui.demo.magnagerImpl.DemoUserState.BindRealName;
import static com.zhuguohui.demo.magnagerImpl.DemoUserState.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.LayoutInflaterCompat;

import com.zhuguohui.demo.databinding.ActivityMainBinding;
import com.zhuguohui.demo.magnagerImpl.DemoUserStateManager;
import com.zhuguohui.demo.toast.GZToast;
import com.zhuguohui.demo.userstate.CheckUserState;
import com.zhuguohui.demo.userstate.UserStateCheckUtil;
import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;


public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //注入factory2
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), UserStateCheckUtil.getLayoutInflaterFactory(this,compositeDisposable, e->{
            GZToast.error().show(e.getMessage());
        }));

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        binding.tvRest.setOnClickListener(v-> {
            DemoUserStateManager.getInstance().reset();
            GZToast.show("重置成功");
        });
        aopActivity();
        aopView();

    }

    private void aopActivity() {
        binding.setActivity(UserStateCheckUtil.getProxy(compositeDisposable,this,this,e->{
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

    @CheckUserState(states = Login|BindPhoneNumber|BindRealName,policy = UserStateCheckPolicy.autoGo)
    public void doCollection(View view){
        GZToast.success().show("收藏成功");
    }
}