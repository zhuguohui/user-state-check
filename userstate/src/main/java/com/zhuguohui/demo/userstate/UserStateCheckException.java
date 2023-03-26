package com.zhuguohui.demo.userstate;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/2/28
 * Time: 15:54
 * Desc:用户状态不匹配的错误
 * </pre>
 */
public class UserStateCheckException extends RuntimeException {
    private final IUserState state;

    public UserStateCheckException(IUserState userState) {
        super("该功能需要" + userState.getDesc());
        this.state = userState;
    }

    public IUserState getState() {
        return state;
    }
}
