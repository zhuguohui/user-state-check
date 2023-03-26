package com.zhuguohui.demo.toast;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.zhuguohui.demo.R;


/**
 * Created by zhuguohui
 * Date: 2023/3/7
 * Time: 20:21
 * Desc:
 */
public class DemoToastImpl implements DemoToastUI {
    private static Handler handler = new Handler(Looper.getMainLooper());

    private  boolean loopShow;
    private  Toast toast;
    private  ImageView ivIcon;
    private  TextView textView;
    private static boolean toastIsShow;
    private Context context;



    @Override
    public void show(Activity activity, GZToast.ToastConfig config, String msg) {
        if (toast == null) {
            createToast(activity);
        }
        Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(), config.type.imageId, null);
        boolean loop = config.type.loop;
        if (loop) {
            drawable = new LoadingDrawable(drawable);
        }

        ivIcon.setImageDrawable(drawable);
        textView.setText(msg);
        toast.setDuration(config.time.value);
        toast.setGravity(Gravity.CENTER, 0, 0);
        if (!toastIsShow) {
            toast.show();
        }
        if (config.type.loop) {
            ((LoadingDrawable) drawable).start();
        }
        loopShow = loop;
    }

    private  void createToast(Activity activity) {
        toast = new Toast(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.gz_toast_layout, null);
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                toastIsShow = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                toastIsShow = false;
                if (loopShow) {
                    handler.post(() -> {
                        toastIsShow=true;
                        toast.show();
                    });
                }
            }
        });
        ivIcon = view.findViewById(R.id.iv_icon);
        textView = view.findViewById(R.id.tv_msg);
        toast.setView(view);
    }
}
