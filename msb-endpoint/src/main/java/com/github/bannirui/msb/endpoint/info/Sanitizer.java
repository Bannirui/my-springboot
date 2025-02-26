package com.github.bannirui.msb.endpoint.info;

import java.util.regex.Pattern;
import org.springframework.util.Assert;

public class Sanitizer {
    private static final String[] REGEX_PARTS = new String[]{"*", "$", "^", "+"};
    private Pattern[] keysToSanitize;

    public Sanitizer() {
        this(".*password.*", ".*pass.*", ".*pwd.*", ".*secret.*", "key", ".*token.*", ".*credentials.*", "vcap_services", "sun.java.command");
    }

    public Sanitizer(String... keysToSanitize) {
        this.setKeysToSanitize(keysToSanitize);
    }

    public void setKeysToSanitize(String... keysToSanitize) {
        Assert.notNull(keysToSanitize, "KeysToSanitize must not be null");
        this.keysToSanitize = new Pattern[keysToSanitize.length];
        for(int i = 0; i < keysToSanitize.length; ++i) {
            this.keysToSanitize[i] = this.getPattern(keysToSanitize[i]);
        }
    }

    private Pattern getPattern(String value) {
        return this.isRegex(value) ? Pattern.compile(value, 2) : Pattern.compile(".*" + value + "$", 2);
    }

    private boolean isRegex(String value) {
        for (String part : REGEX_PARTS) {
            if (value.contains(part)) {
                return true;
            }
        }
        return false;
    }

    public Object sanitize(String key, Object value) {
        if (value == null) {
            return null;
        }
        for (Pattern pattern : this.keysToSanitize) {
            if (pattern.matcher(key).matches()) {
                return "******";
            }
        }
        return value;
    }
}
