package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.common.plugin.InterceptorUtil;
import com.github.bannirui.msb.common.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.common.register.BeanDefinition;
import com.github.bannirui.msb.mq.sdk.MmsMsbImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * msb接入mq.
 */
public class MMSConfiguration extends AbstractBeanRegistrar {
    @Override
    public void registerBeans() {
        // construct the instance via constructor, indicate the mq name server(zookeeper) 封装mq元数据信息
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(MmsMsbImpl.class).addConstructorArgValue(this.getProperty("mms.nameServerAddress")));
        // 缓存监听器配置
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(MMSListenerInitialization.class));
        // 订阅mq
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(MMSSubscribeEventListener.class));
    }

    /**
     * 上面已经把BeanDefinition加载到了Spring中 现在把自定义Bean放到容器中
     * 创建mq的生产者跟消费者2个bean
     * <ul>
     *     <li>mmsTemplate</li>
     *     <li>mmsSubscribeTemplate</li>
     * </ul>
     * @param beanFactory the bean factory used by the application context
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // msb配置
        String mmsNameServerAddress = this.getProperty("mms.nameServerAddress");
        if(StringUtils.isEmpty(mmsNameServerAddress)) {
            throw FrameworkException.getInstance("MQ消息服务初始化失败-mmsNameServerAddress为空，请检查配置");
        }
        // Spring单例
        MmsMsbImpl mmsMsb = beanFactory.getBean(MmsMsbImpl.class);
        MMSTemplate MMSTemplateProxyObj = null;
        try {
            // MMSTemplate的代理 生产者
            MMSTemplateProxyObj = InterceptorUtil.getProxyObj(MMSTemplate.class, new Class[]{MmsMsbImpl.class}, new Object[]{mmsMsb}, "MMS.Producer");
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "加载MMS-MQ提供者监听插件失败,errorMessage{0}", e);
        }
        // MMSTemplate的代理对象注入Spring
        beanFactory.registerSingleton("mmsTemplate", MMSTemplateProxyObj);
        MMSSubscribeTemplate MMSSubscribeTemplateProxyObj = null;
        try {
            // MMSSubscribeTemplate的代理 消费者
            MMSSubscribeTemplateProxyObj = InterceptorUtil.getProxyObj(MMSSubscribeTemplate.class, new Class[]{MmsMsbImpl.class}, new Object[]{mmsMsb}, "MMS.Subscribe");
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "加载MMS-MQ订阅着监听插件失败,errorMessage{0}", e);
        }
        // MMSSubscribeTemplate的代理注入Spring
        beanFactory.registerSingleton("mmsSubscribeTemplate", MMSSubscribeTemplateProxyObj);
    }
}
