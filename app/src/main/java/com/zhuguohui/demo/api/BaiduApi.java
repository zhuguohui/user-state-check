package com.zhuguohui.demo.api;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 21:27
 * Desc:
 */
public interface BaiduApi {

    /**
     * 获取百度热榜
     * @return
     */
    @GET("https://tenapi.cn/v2/baiduhot")
    Observable<BaiduHotListResult> getHotList();



}
