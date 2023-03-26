package com.zhuguohui.demo.userstate;

import okhttp3.Request;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/14
 * Time: 17:13
 * Desc:用户实现在用户登录以后，替换用户信息的工作。
 * </pre>
 */
public interface UserInfoUpdate {
     Request updateUserInfo(Request oldRequest);
}
