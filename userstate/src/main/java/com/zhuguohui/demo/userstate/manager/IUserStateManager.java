package com.zhuguohui.demo.userstate.manager;

import android.app.Activity;
import android.content.Context;

import com.zhuguohui.demo.userstate.IUserState;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 13:27
 * Desc:用户状态管理的接口
 * 定义成接口方便不同的项目实现具体的类
 * </pre>
 */
public  interface  IUserStateManager {
    /**
     * 是否达到了用户状态
     * @param userState
     * @return
     */
    boolean isMatchUserState(IUserState userState);

    /**
     * 执行相应的请求
     * @param state
     * @return
     */
    void doMatchUserState(Context context,IUserState state);

    /**
     * 用于判断 当前的页面的用途，因为每种用户状态的确认可能涉及多个页面
     * 比如登录可能涉及到登录和注册两个页面。只有相关的页面都销毁了。
     * 才可以判断是否登录成功，回调相关的callback
     * @param activity
     * @return 如果当前页面和登录相关返回 {@link IUserState#login}
     *         以此类推，如果都不相关，返回null。
     */
    IUserState[] getActivityUserStateType(Activity activity);

    /**
     * 通过flags获取用户状态
     * @param flags
     * @return
     */
    IUserState[] getUserStateByFlags(int flags);
}
