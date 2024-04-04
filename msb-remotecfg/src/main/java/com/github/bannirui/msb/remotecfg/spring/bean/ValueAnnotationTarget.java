package com.github.bannirui.msb.remotecfg.spring.bean;

/**
 * 注解作用在成员字段还是方法上.
 */
public interface ValueAnnotationTarget {

    enum TargetType {FIELD, METHOD;}

    TargetType getTargetType();
}
