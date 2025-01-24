package com.github.bannirui.msb.mq.annotation;

import com.github.bannirui.msb.mq.enums.MQMsgEnum;
import com.github.bannirui.msb.mq.enums.Serialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 搭配{@link MMSListener}使用 标识mq监听器的方法参数
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MMSListenerParameter {
    /**
     * 监听器方法参数映射mq消息属性字段
     */
    MQMsgEnum name();

    Serialize serialize() default Serialize.STRING;
}
