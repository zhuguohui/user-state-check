package com.zhuguohui.demo.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 21:29
 * Desc:
 */
public class BaiduHotListResult {

    private Integer code;
    private String msg;
    private List<HotItemBean> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<HotItemBean> getData() {
        return data;
    }

    public void setData(List<HotItemBean> data) {
        this.data = data;
    }
}
