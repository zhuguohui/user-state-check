package com.zhuguohui.demo.impl;

import android.app.Activity;
import android.content.Context;

import com.zhuguohui.demo.ui.OptionActivity;
import com.zhuguohui.demo.userstate.IUserStatePage;
import com.zhuguohui.demo.userstate.IUserState;
import com.zhuguohui.demo.userstate.manager.IUserStateManager;

import java.util.HashMap;
import java.util.Map;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 14:50
 * Desc:
 * </pre>
 */
public class DemoUserStateManager implements IUserStateManager {

    Map<IUserState,Boolean> userStateMap=new HashMap<>();

    static DemoUserStateManager instance=new DemoUserStateManager();

    public static DemoUserStateManager getInstance() {
        return instance;
    }

    private DemoUserStateManager(){

    }

    @Override
    public boolean isMatchUserState(IUserState userState) {
        Boolean aBoolean = userStateMap.get(userState);
        return aBoolean!=null&& aBoolean;
    }


    @Override
    public void doMatchUserState(Context context,IUserState state) {
        OptionActivity.openForState(context,state);
    }

    @Override
    public IUserState[] getActivityUserStateType(Activity activity) {
        if (activity instanceof IUserStatePage) {
            IUserStatePage pageInfo = (IUserStatePage) activity;
            return pageInfo.getUserSatePageTypes();
        }
        return null;
    }

    @Override
    public IUserState[] getUserStateByFlags(int flags) {
     return DemoUserState.getUserStateByFlags(flags);
    }

    public void matchUserStateSuccess(IUserState userState) {
        userStateMap.put(userState,true);
    }

    public void reset() {
        userStateMap.clear();
    }
}
