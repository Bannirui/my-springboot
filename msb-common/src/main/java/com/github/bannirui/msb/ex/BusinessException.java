package com.github.bannirui.msb.ex;

import java.text.MessageFormat;
import java.util.Objects;

public class BusinessException extends BaseException {
    public static final String ERR_DEF = "DEF002";

    public BusinessException(String code, String msg) {
        super(code, msg);
    }

    public static BusinessException getInstance(String pattern, Object... args) {
        String msg = MessageFormat.format(pattern, args);
        return new BusinessException(ERR_DEF, msg);
    }

    public static BusinessException getInstance(Throwable e, String pattern, Object... args) {
        String msg = MessageFormat.format(pattern, args);
        return new BusinessException(ERR_DEF, msg, e);
    }

    public BusinessException(String code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public BusinessException(Throwable e, String msg) {
        super(ERR_DEF, msg, e);
    }

    public BusinessException(Throwable e, String msg, Object... args) {
        super(ERR_DEF, msg, e);
    }

    public static boolean isBusinessException(Throwable e) {
        return Objects.isNull(e) ? false : BusinessException.class.isAssignableFrom(e.getClass()) || isDubboBusinessException(e) ||
            BusinessExceptionMarkable.class.isAssignableFrom(e.getClass());
    }

    private static boolean isDubboBusinessException(Throwable e) {
        return Objects.nonNull(e.getMessage()) && e.getMessage().startsWith("com.github.bannirui.common.ex.BusinessException") &&
            RuntimeException.class.isAssignableFrom(e.getClass());
    }
}
