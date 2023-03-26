package com.zhuguohui.demo.api;

import android.app.Application;

import com.zhuguohui.demo.magnagerImpl.DemoUserStateManager;
import com.zhuguohui.demo.userstate.manager.UserStateManager;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 11:18
 * Desc:
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserStateManager.getInstance().setUserStateManagerImpl(DemoUserStateManager.getInstance());
    }
}
