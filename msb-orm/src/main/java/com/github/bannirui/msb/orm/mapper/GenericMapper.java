package com.github.bannirui.msb.orm.mapper;

import tk.mybatis.mapper.common.Mapper;

public interface GenericMapper<T> extends Mapper<T>, OptimisticLockerUpdateMapper<T> {
}
