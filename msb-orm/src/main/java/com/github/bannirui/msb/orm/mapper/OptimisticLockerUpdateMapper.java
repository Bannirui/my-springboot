package com.github.bannirui.msb.orm.mapper;

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
