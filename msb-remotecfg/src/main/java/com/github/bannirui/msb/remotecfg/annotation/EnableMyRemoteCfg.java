package com.github.bannirui.msb.remotecfg.annotation;

import com.github.bannirui.msb.remotecfg.EnableRemoteCfgAnnotationCheck;
import com.github.bannirui.msb.remotecfg.autoconfig.MyRemoteCfgImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * 注解启动远程配置中心场景.
 * 远程配置中心的拉取跟Spring声明周期紧密关联.
 * 在Bean实例化的时候成员属性就需要获取到远程配置中心的配置项 但是又要依赖当前注解的属性传递配置中心的信息 所以拉取时机只能选择在{@link org.springframework.boot.context.event.ApplicationPreparedEvent}事件发布的时机.
 * 彼时Spring上下文准备就绪 只有业务的主配置类 其他Bean还没实例化好.
 * 因此这个注解必须打在主配置类上{@link EnableRemoteCfgAnnotationCheck}.
 * </p>
 * <p>
 * 检查事项为
 * <ul>
 *     <li>启动上一定有该注解</li>
 *     <li>该注解只能打在启动类上</li>
 * </ul>
 * </p>
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({MyRemoteCfgImportSelector.class})
public @interface EnableMyRemoteCfg {

    /**
     * nacos的dataId.
     */
    String[] dataId();
}
