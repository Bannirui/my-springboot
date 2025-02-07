package com.github.bannirui.msb.orm.util;

import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceHelp {

    public static Resource[] resolveMapperLocations(String locations) {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<>();
        if (locations != null) {
            String[] mapperLocationArray = locations.split(",");
            for (String mapperLocation : mapperLocationArray) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    throw new ErrorCodeException(e, ExceptionEnum.FILE_EXCEPTION, "Mybatis搜索资源文件", mapperLocation);
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }
}
