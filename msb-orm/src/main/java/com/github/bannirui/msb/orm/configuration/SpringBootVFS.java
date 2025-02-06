package com.github.bannirui.msb.orm.configuration;

public class SpringBootVFS extends VFS {
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

    public SpringBootVFS() {
    }

    public boolean isValid() {
        return true;
    }

    protected List<String> list(URL url, String path) throws IOException {
        String urlString = url.toString();
        String baseUrlString = urlString.endsWith("/") ? urlString : urlString.concat("/");
        Resource[] resources = this.resourceResolver.getResources(baseUrlString + "**/*.class");
        return (List)Stream.of(resources).map((resource) -> {
            return preserveSubpackageName(baseUrlString, resource, path);
        }).collect(Collectors.toList());
    }

    private static String preserveSubpackageName(final String baseUrlString, final Resource resource, final String rootPath) {
        try {
            return rootPath + (rootPath.endsWith("/") ? "" : "/") + resource.getURL().toString().substring(baseUrlString.length());
        } catch (IOException var4) {
            throw new UncheckedIOException(var4);
        }
    }
}
