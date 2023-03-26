package com.zhuguohui.demo.magnagerImpl;

import android.util.Log;

import com.zhuguohui.demo.userstate.IUserState;

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


}
