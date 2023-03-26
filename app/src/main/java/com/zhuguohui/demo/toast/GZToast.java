package com.zhuguohui.demo.toast;

import android.widget.Toast;

import com.zhuguohui.demo.R;
import com.zhuguohui.demo.ui.BaseActivity;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/7
 * Time: 10:40
 * Desc:根据设计规范统一使用的Toast工具
 * </pre>
 */
public class GZToast {




    public static ToastConfig success() {
        return new ToastConfig().success();
    }

    public static ToastConfig error() {
        return new ToastConfig().error();
    }

    public static ToastConfig loading() {
        return new ToastConfig().loading();
    }

    public static ToastConfig waring() {
        return new ToastConfig().warning();
    }



    public static void show(String msg) {
        show(new ToastConfig(), msg);
    }

    private static DemoToastUI gzToastUI;
    public static void show(ToastConfig config, String msg) {
       if(gzToastUI==null){
           gzToastUI=new DemoToastImpl();
       }
       gzToastUI.show(BaseActivity.getCurrentActivity(),config,msg);

    }




    public static class ToastConfig {
        enum ToastType {
            success(R.drawable.gz_ic_toast_success),
            loading(R.drawable.gz_ic_toast_loading, true),
            error(R.drawable.gz_ic_toast_error),
            warning(R.drawable.gz_ic_toast_warring);

            int imageId;
            boolean loop;

            ToastType(int imageId) {
                this(imageId, false);
            }

            ToastType(int imageId, boolean loop) {
                this.imageId = imageId;
                this.loop = loop;
            }
        }

        enum ToastTime {
            Long(Toast.LENGTH_LONG),
            Short(Toast.LENGTH_SHORT);

            ToastTime(int value) {
                this.value = value;
            }

            int value;

        }


        ToastTime time = ToastTime.Short;
        ToastType type = ToastType.success;


        public void show(String msg) {
            GZToast.show(this, msg);
        }

        public ToastConfig success() {
            type = ToastType.success;
            return this;
        }

        public ToastConfig error() {
            type = ToastType.error;
            return this;
        }

        public ToastConfig warning() {
            type = ToastType.warning;
            time = ToastTime.Long;
            return this;
        }

        public ToastConfig loading() {
            type = ToastType.loading;
            return this;
        }

        public ToastConfig longTime() {
            time = ToastTime.Long;
            return this;
        }

        public ToastConfig shortTime() {
            time = ToastTime.Short;
            return this;
        }
    }
}
