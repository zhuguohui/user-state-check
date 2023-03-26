package com.zhuguohui.demo.toast;

import android.app.Activity;

/**
 * Created by zhuguohui
 * Date: 2023/3/7
 * Time: 20:19
 * Desc:
 */
public interface DemoToastUI {

     void show(Activity activity,GZToast.ToastConfig config, String msg);
}
