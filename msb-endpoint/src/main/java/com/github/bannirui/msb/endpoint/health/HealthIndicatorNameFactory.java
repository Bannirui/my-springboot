package com.github.bannirui.msb.endpoint.health;

import java.util.Locale;

public class HealthIndicatorNameFactory {

    public String apply(String name) {
        int index = name.toLowerCase(Locale.ENGLISH).indexOf("healthindicator");
        return index > 0 ? name.substring(0, index) : name;
    }
}
