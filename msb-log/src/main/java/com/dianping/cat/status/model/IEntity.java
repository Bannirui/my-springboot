package com.dianping.cat.status.model;

public interface IEntity<T> {
    void accept(IVisitor var1);

    void mergeAttributes(T var1);
}
