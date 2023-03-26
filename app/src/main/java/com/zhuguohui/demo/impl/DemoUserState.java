package com.zhuguohui.demo.impl;

import android.util.Log;

import com.zhuguohui.demo.userstate.IUserState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 10:05
 * Desc:
 */
public final class DemoUserState extends IUserState {
    private static final DemoUserState login=new DemoUserState("登录",1);
    private static final DemoUserState bindPhoneNumber=new DemoUserState("绑定手机号",2);
    private static final DemoUserState bindRealName=new DemoUserState("实名认证",4);

    public static final int Login=1;
    public static final int BindPhoneNumber=2;
    public static final int BindRealName=4;


    public static final DemoUserState[] values=new DemoUserState[]{login,bindPhoneNumber,bindRealName};
    protected DemoUserState(String desc, int attrFlagValue) {
        super(desc, attrFlagValue);
        Log.d("zzz", "DemoUserState() called with: desc = [" + desc + "], attrFlagValue = [" + attrFlagValue + "]");
    }


    public static IUserState[] getUserStateByFlags(int flags) {
        DemoUserState[] values = DemoUserState.values;
        List<DemoUserState> stateList=new ArrayList<>(0);
        for(DemoUserState state:values){
            boolean match =( flags & state.getAttrFlagValue()) == state.getAttrFlagValue();
            if(match){
                stateList.add(state);
            }
        }

        return stateList.toArray(new IUserState[0]);
    }
}
