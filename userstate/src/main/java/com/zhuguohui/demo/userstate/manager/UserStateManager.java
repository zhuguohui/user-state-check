package com.zhuguohui.demo.userstate.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;


import com.zhuguohui.demo.userstate.IUserState;
import com.zhuguohui.demo.userstate.UserStateCallBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


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


    public void onActivityPostCreate(Activity activity) {
        IUserState[] states = iUserStateManager.getActivityUserStateType(activity);
        if (isRequestStatePage(states)) {
            activityCount.incrementAndGet();
        }
    }

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
}
