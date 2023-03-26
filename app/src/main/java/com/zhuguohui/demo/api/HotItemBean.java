package com.zhuguohui.demo.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 21:30
 * Desc:
 */
public class HotItemBean {
    private String name;
    private String hot;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHot() {
        return hot;
    }

    public void setHot(String hot) {
        this.hot = hot;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
