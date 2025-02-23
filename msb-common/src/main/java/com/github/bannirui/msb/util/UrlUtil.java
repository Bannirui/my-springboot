package com.github.bannirui.msb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {
    public static final Pattern domainPattern = Pattern.compile("(http|https)://(www.)?([\\w|-]+(\\.)?)+");

    public static String retrieveDomainFromUrl(String url) {
        Matcher m = domainPattern.matcher(url);
        if (m.find()) {
            String urlWithSchema = m.group(0);
            String schema = m.group(1);
            return urlWithSchema.replace(schema + "://", "");
        } else {
            return null;
        }
    }
}
