package com.github.bannirui.msb.orm.configuration;

import org.apache.ibatis.io.VFS;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringBootVFS extends VFS {
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected List<String> list(URL url, String path) throws IOException {
        String urlString = url.toString();
        String baseUrlString = urlString.endsWith("/") ? urlString : urlString.concat("/");
        Resource[] resources = this.resourceResolver.getResources(baseUrlString + "**/*.class");
        return Stream.of(resources).map((resource) -> preserveSubpackageName(baseUrlString, resource, path)).collect(Collectors.toList());
    }

    private static String preserveSubpackageName(final String baseUrlString, final Resource resource, final String rootPath) {
        try {
            return rootPath + (rootPath.endsWith("/") ? "" : "/") + resource.getURL().toString().substring(baseUrlString.length());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
