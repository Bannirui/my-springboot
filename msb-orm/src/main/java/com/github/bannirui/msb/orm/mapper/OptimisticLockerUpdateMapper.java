package com.github.bannirui.msb.orm.mapper;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

public interface OptimisticLockerUpdateMapper<T> {
    @UpdateProvider(
        type = OptimisticLockerUpdateProvider.class,
        method = "dynamicSQL"
    )
    @Options(
        useCache = false,
        useGeneratedKeys = false
    )
    int updateByExampleAndVersionSelective(@Param("record") T record, @Param("example") Object example);
}
