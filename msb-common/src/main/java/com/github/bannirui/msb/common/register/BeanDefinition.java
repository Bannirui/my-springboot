package com.github.bannirui.msb.common.register;

import com.github.bannirui.msb.common.enums.ExceptionEnum;
import com.github.bannirui.msb.common.ex.ErrorCodeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDefinition<T> {
    private Class<T> beanClass;
    private String beanName;
    private List<String> dependsOnList;
    private Map<String, Object> properties;
    private Map<String, String> propertiesBeanNames;
    private List<Object> constructorArgumentValues;
    private List<String> constructorArgumentBeanNames;
    private String initMethodName;
    private String destroyMethodName;

    private BeanDefinition() {
    }

    public static <T> BeanDefinition<T> newInstance(Class<T> t) {
        BeanDefinition<T> beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(t);
        return beanDefinition;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public BeanDefinition setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public Class<T> getBeanClass() {
        return this.beanClass;
    }

    public BeanDefinition setBeanClass(Class<T> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    public BeanDefinition addNullPropertyValue(String name) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }

        this.properties.put(name, (Object) null);
        return this;
    }

    public BeanDefinition addPropertyValue(String name, Object value) {
        if (value == null) {
            return this;
        } else {
            if (this.properties == null) {
                this.properties = new HashMap();
            }

            this.properties.put(name, value);
            return this;
        }
    }

    public BeanDefinition addPropertyReference(String name, String beanName) {
        if (this.propertiesBeanNames == null) {
            this.propertiesBeanNames = new HashMap();
        }

        this.propertiesBeanNames.put(name, beanName);
        return this;
    }

    public BeanDefinition addNullConstructorArgValue() {
        if (this.constructorArgumentValues == null) {
            this.constructorArgumentValues = new ArrayList();
        }

        this.constructorArgumentValues.add((Object) null);
        return this;
    }

    public BeanDefinition addConstructorArgValue(Object... value) {
        if (this.constructorArgumentValues == null) {
            this.constructorArgumentValues = new ArrayList();
        }

        if (value == null) {
            return this;
        } else {
            Object[] var2 = value;
            int var3 = value.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object o = var2[var4];
                this.constructorArgumentValues.add(o);
            }

            return this;
        }
    }

    public BeanDefinition addConstructorArgReference(String... beanName) {
        if (this.constructorArgumentBeanNames == null) {
            this.constructorArgumentBeanNames = new ArrayList();
        }

        if (beanName == null) {
            throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, new Object[] {"BeanDefinition.addConstructorArgValue", beanName});
        } else {
            String[] var2 = beanName;
            int var3 = beanName.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String s = var2[var4];
                this.constructorArgumentBeanNames.add(s);
            }

            return this;
        }
    }

    public List<String> getDependsOnList() {
        return this.dependsOnList;
    }

    public BeanDefinition addDependsOn(String dependsOn) {
        if (this.dependsOnList == null) {
            this.dependsOnList = new ArrayList();
        }

        this.dependsOnList.add(dependsOn);
        return this;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public List<Object> getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    public String getInitMethodName() {
        return this.initMethodName;
    }

    public BeanDefinition setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
        return this;
    }

    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    public BeanDefinition setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
        return this;
    }

    public List<String> getConstructorArgumentBeanNames() {
        return this.constructorArgumentBeanNames;
    }

    public Map<String, String> getPropertiesBeanNames() {
        return this.propertiesBeanNames;
    }
}
