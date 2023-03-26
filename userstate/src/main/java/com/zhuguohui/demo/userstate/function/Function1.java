package com.zhuguohui.demo.userstate.function;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 9:27
 * Desc:定义一个通用接口
 */
public interface Function1<T,R> {
    R call(T t);
}
