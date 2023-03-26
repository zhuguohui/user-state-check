package com.zhuguohui.demo.toast;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;
import android.view.animation.LinearInterpolator;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/7
 * Time: 13:36
 * Desc:
 * </pre>
 */
public class LoadingDrawable extends RotateDrawable {


    private final ObjectAnimator animator;

    public LoadingDrawable(Drawable drawable) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setDrawable(drawable);
        }
        animator = ObjectAnimator.ofInt(this, "level", 0,10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(1000);
        animator.setRepeatCount(-1);

    }

    public void start(){
        animator.start();
    }

    public void cancel(){
        animator.cancel();
    }
}
