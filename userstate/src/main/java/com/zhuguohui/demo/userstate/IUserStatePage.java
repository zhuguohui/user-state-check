package com.zhuguohui.demo.userstate;


import androidx.annotation.Nullable;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 14:07
 * Desc:通常由activity实现。
 * 表示这个界面和用户的状态的流程有关，比如登录，绑定手机号，实名认证
 * 因为一个流程比如登录，可能涉及到多个页面。比如注册，登录，找回密码
 * 只有相关的页面全部finish以后，才去检查是否登录成功，回调相关的方法。
 * </pre>
 */
public interface IUserStatePage {

    /**
     * 返回当前页面和那些流程相关
     * @return 返回相关用户状态的数组，可以为null
     */
   @Nullable
   IUserState[] getUserSatePageTypes();
}
