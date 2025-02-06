package com.github.bannirui.msb.orm.mapper;

public interface GenericMapper<T> extends Mapper<T>, OptimisticLockerUpdateMapper<T> {
}
