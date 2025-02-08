package com.github.bannirui.msb.orm.mapper;

import com.github.pagehelper.PageHelper;
import tk.mybatis.mapper.weekend.Fn;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.reflection.Reflections;

public class Condition<T> extends Weekend<T> {
    public Condition(Class<T> entityClass) {
        super(entityClass);
    }

    public Condition(Class<T> entityClass, boolean exists) {
        super(entityClass, exists);
    }

    public Condition(Class<T> entityClass, boolean exists, boolean notNull) {
        super(entityClass, exists, notNull);
    }

    public Condition<T> selectProperties(Fn<T, Object>... fns) {
        for (Fn<T, Object> fn : fns) {
            this.selectProperties(Reflections.fnToFieldName(fn));
        }
        return this;
    }

    public Condition<T> excludeProperties(Fn<T, Object>... fns) {
        for (Fn<T, Object> fn : fns) {
            this.excludeProperties(Reflections.fnToFieldName(fn));
        }
        return this;
    }

    public void startPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
    }
}
