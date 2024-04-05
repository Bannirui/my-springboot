package com.github.bannirui.msb.remotecfg.spring.bean;

import java.util.List;

public class ValueAnnotatedObj {

    /**
     * 在配置中心上缓存着的最新的值.
     */
    private String lastVal;

    // Value注解Placeholder最用的对象
    List<BeanWithValueAnnotation> list;

    public ValueAnnotatedObj(String lastVal, List<BeanWithValueAnnotation> list) {
        this.lastVal = lastVal;
        this.list = list;
    }

    public String getLastVal() {
        return lastVal;
    }

    public List<BeanWithValueAnnotation> getList() {
        return list;
    }

    public void setLastVal(String lastVal) {
        this.lastVal = lastVal;
    }
}
