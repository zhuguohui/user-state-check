package com.zhuguohui.demo.userstate.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;


import androidx.appcompat.app.AppCompatActivity;

import com.zhuguohui.demo.userstate.IUserState;
import com.zhuguohui.demo.userstate.UserStateCallBack;
import com.zhuguohui.demo.userstate.function.CallBack;
import com.zhuguohui.demo.userstate.transform.UserStateTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/2/28
 * Time: 15:38
 * Desc:
 * </pre>
 */
public class UserStateManager {
    private Map<IUserState, List<UserStateCallBack>> callBackMap = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private IUserState requestMatchState;
    private IUserStateManager iUserStateManager;
    private static UserStateManager instance = new UserStateManager();
    private AtomicInteger activityCount = new AtomicInteger(0);
    private Map<IUserState,Long> requestTimeMap=new HashMap<>();
    public static UserStateManager getInstance() {
        return instance;
    }


    public void setUserStateManagerImpl(IUserStateManager iUserStateManager) {
        this.iUserStateManager = iUserStateManager;
    }


    public boolean isMatchUserState(IUserState userState) {
        return iUserStateManager.isMatchUserState(userState);
    }

    static final UserStateCallBack emptyCallBack = new UserStateCallBack() {
        @Override
        public void onMath() {

        }

        @Override
        public void onCancel() {

        }
    };

    public void doMatchUserState(Context context,IUserState state,  UserStateCallBack function) {
        if (function == null) {
            function = emptyCallBack;
        }
        Long aLong = requestTimeMap.get(state);
        long lastRequest=0;
        if(aLong!=null){
            lastRequest=aLong;
        }
        long passTime = System.currentTimeMillis() - lastRequest;
        if(passTime<3000){
            //请求太频繁了
            function.onCancel();
            return;
        }
        requestTimeMap.put(state,System.currentTimeMillis());

        List<UserStateCallBack> callBacks = callBackMap.get(state);
        if (callBacks == null) {
            callBacks = new ArrayList<>();
            callBackMap.put(state, callBacks);
        }
        callBacks.add(function);
        requestMatchState = state;
        //   BaseActivity.getCurrentActivity().startActivity(new Intent(BaseActivity.getCurrentActivity(), LoginActivity.class));
        iUserStateManager.doMatchUserState(context,state);
    }

    /**
     * 在activity的onPostCreate方法中回调
     * 注意是参数签名为以下的方法，只有一个参数，还有一个同名的方法，有两个参数，不要搞错了
     * protected void onPostCreate(@Nullable Bundle savedInstanceState)
     * @param activity
     */
    public void onActivityPostCreate(Activity activity) {
        IUserState[] states = iUserStateManager.getActivityUserStateType(activity);
        if (isRequestStatePage(states)) {
            activityCount.incrementAndGet();
        }
    }

    /**
     * 在activity销毁的时候回调，用于触发相关的callBack
     * @param activity
     */
    public void onActivityDestroy(Activity activity) {
        IUserState[] userStates = iUserStateManager.getActivityUserStateType(activity);
        if (isRequestStatePage(userStates)) {
            int count = activityCount.decrementAndGet();
            if (count == 0) {
                handler.post(() -> matchCallBack(requestMatchState, isMatchUserState(requestMatchState)));
            }
        }


    }

    private boolean isRequestStatePage(IUserState[] userStates) {
        if (userStates != null) {
            return Arrays.asList(userStates).contains(requestMatchState);
        }
        return false;
    }


    private void matchCallBack(IUserState state, boolean success) {
        List<UserStateCallBack> callBacks = callBackMap.get(state);
        if (callBacks != null) {
            for (UserStateCallBack callBack : callBacks) {
                if (success) {
                    callBack.onMath();
                } else {
                    callBack.onCancel();
                }
            }
            callBacks.clear();
        }
    }

    public IUserState[] getUserStateByFlags(int states) {
        return iUserStateManager.getUserStateByFlags(states);
    }

    /**
     * 检测用户状态如果用户状态不匹配直接报错
     * {@link com.zhuguohui.demo.userstate.UserStateCheckException}
     * @param context 上下文
     * @param userStateFlags 用户状态的flag
     * @return 如果匹配向下游发送一个object ，否则直接报错
     */
    public static Observable<Object> justCheck(Context context, int userStateFlags) {
        return UserStateCheckUtil.justCheck(context,userStateFlags);
    }

    /**
     * 检测状态，状态如果不满足，自动跳转
     * @param context 上下文
     * @param userStateFlags 状态的flags
     * @return 返回一个Observable 如果发射数据表示状态检测通过。
     */
    public static Observable<Object> checkAndGoto(Context context,int userStateFlags) {
        return UserStateCheckUtil.checkAndGoto(context,userStateFlags);
    }

    /**
     * 获取代理类，该类为 delegate的子类
     * 并且自会重写具有{@link com.zhuguohui.demo.userstate.CheckUserState}注解的方法
     * 自动插入用户状态检测的逻辑
     * @param compositeDisposable 用于取消请求
     * @param context 上下文
     * @param delegate 被代理的类
     * @param errorFunction 出错时的回调
     * @param <T> delegate的类型
     * @return 返回delegate的子类对象
     */
    public static <T> T getProxy(CompositeDisposable compositeDisposable, Context context, T delegate, CallBack<Throwable> errorFunction) {
        return UserStateCheckUtil.getProxy(compositeDisposable,context,delegate,errorFunction);
    }

    /**
     * 获取具有状态检测功能的 LayoutInflater.Factory2
     * @param activity 要作用的activity
     * @param compositeDisposable 用于取消请求
     * @param errorFunction 出错时的回调
     * @return LayoutInflater.Factory2
     */
    public static LayoutInflater.Factory2 getLayoutInflaterFactory(AppCompatActivity activity, CompositeDisposable compositeDisposable, CallBack<Throwable> errorFunction) {
            return UserStateCheckUtil.getLayoutInflaterFactory(activity,compositeDisposable,errorFunction);
    }
}
