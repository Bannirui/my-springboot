package com.github.bannirui.msb.common.ex;

import java.text.MessageFormat;

/**
 * 自定义异常 框架层面.
 */
public class FrameworkException extends BaseException {

    public static final String ERR_DEF = "DEF001";

    public FrameworkException(String code, String msg) {
        super(code, msg);
    }

    public static FrameworkException getInstance(String pattern, Object... args) {
        String msg = MessageFormat.format(pattern, args);
        return new FrameworkException(ERR_DEF, msg);
    }

    public static FrameworkException getInstance(Throwable e, String pattern, Object... args) {
        String msg = MessageFormat.format(pattern, args);
        return new FrameworkException(ERR_DEF, msg, e);
    }

    public FrameworkException(String code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public FrameworkException(Throwable e, String msg) {
        super(ERR_DEF, msg, e);
    }

    public FrameworkException(Throwable e, String msg, Object... args) {
        super(ERR_DEF, msg, e);
    }
}
