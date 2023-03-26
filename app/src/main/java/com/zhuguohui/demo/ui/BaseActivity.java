package com.zhuguohui.demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

import com.zhuguohui.demo.toast.GZToast;
import com.zhuguohui.demo.userstate.UserStateCheckUtil;
import com.zhuguohui.demo.userstate.manager.UserStateManager;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 10:50
 * Desc:
 */
public class BaseActivity extends AppCompatActivity {

  private   static Activity currentActivity;
    protected CompositeDisposable compositeDisposable=new CompositeDisposable();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        currentActivity=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity=this;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UserStateManager.getInstance().onActivityPostCreate(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserStateManager.getInstance().onActivityDestroy(this);
        compositeDisposable.dispose();
    }
}
