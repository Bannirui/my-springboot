package com.github.bannirui.msb.orm.util;

public class ResourceHelp {

    public static Resource[] resolveMapperLocations(String locations) {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList();
        if (locations != null) {
            String[] mapperLocationArray = locations.split(",");
            String[] var4 = mapperLocationArray;
            int var5 = mapperLocationArray.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String mapperLocation = var4[var6];

                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException var9) {
                    throw new ErrorCodeException(var9, ExceptionEnum.FILE_EXCEPTION, new Object[]{"Mybatis搜索资源文件", mapperLocation});
                }
            }
        }

        return (Resource[])resources.toArray(new Resource[resources.size()]);
    }
}
