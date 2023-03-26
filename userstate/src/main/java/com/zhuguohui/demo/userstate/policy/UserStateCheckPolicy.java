package com.zhuguohui.demo.userstate.policy;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/2/28
 * Time: 17:52
 * Desc:用户状态的匹配策略
 * 因为有些界面，比如一个列表需要登录才能查看
 * 但是滑动到列表的时候，如果每次都自动弹出登录界面
 * 用户体验不好。
 * 这个时候就可以用 {@link #justCheck}来实现只检查状态
 * 不自动跳转。
 * 如果状态不正确会抛出错误{@link com.zhuguohui.demo.userstate.UserStateCheckException}
 * 通过判断错误的类型就可以显示自定义的UI
 * </pre>
 */
public enum UserStateCheckPolicy {
    autoGo,//自动匹配，弹出登录界面等
    justCheck//抛出错误,不弹出登录界面。
}
