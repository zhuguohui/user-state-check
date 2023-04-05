package com.zhuguohui.demo.userstate;

import java.io.Serializable;
import java.util.Objects;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/2/28
 * Time: 16:32
 * Desc:用户状态的鸡肋
 * </pre>
 */
public  class IUserState implements Serializable {


   protected IUserState(String desc, int flag) {
        this.desc = desc;
        this.flag = flag;
    }


    private final String desc;
    private final int flag;

    /**
     * 获取xml中对应属性的int值
     * @return
     */
    public int getAttrFlagValue() {
        return flag;
    }

    /**
     * 获取描述便于提示
     * @return
     */
    public String getDesc() {
        return desc;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IUserState userState = (IUserState) o;
        return flag == userState.flag && Objects.equals(desc, userState.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, flag);
    }
}
