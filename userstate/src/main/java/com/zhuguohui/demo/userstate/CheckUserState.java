package com.zhuguohui.demo.userstate;


import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 11:40
 * Desc:
 * 用于检查用户状态
 * 需要在Retrofit相关的API接口的方法上使用
 * 其中的value是需要检查的状态 参考{@link IUserState}
 * 注解通过 {@link UserStateCheckIntercept}这个拦截器处理
 * 需要在Retrofit的OKHttpClient中添加该拦截器。
 *
 * 如果需要配置{@link com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy}
 * 可以通过 添加@Tag 注解来实现。
 *
 * 示例代码如下
 *<code>
 *     public interface TestApi {
 *
 *     &#64;CheckUserState({UserState.login,
 *     UserState.bindPhoneNumber,
 *     UserState.bindRealName})
 *     &#64;GET("channels.json")
 *     Observable<ChannelsBean> getRoot(@Tag UserStateCheckPolicy policy);
 *
 * }
 *</code>
 *
 * </pre>
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUserState {
    int states();
    UserStateCheckPolicy policy() default UserStateCheckPolicy.justCheck;

}
