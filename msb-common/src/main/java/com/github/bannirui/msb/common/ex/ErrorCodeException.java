package com.github.bannirui.msb.common.ex;

import com.github.bannirui.msb.common.enums.ErrorCode;
import java.text.MessageFormat;

/**
 * 自定义异常.
 */
public class ErrorCodeException extends BaseException {

    public ErrorCodeException(ErrorCode code, Object... args) {
        super(code.getCode(), MessageFormat.format(code.getMsg(), args));
    }

    public ErrorCodeException(Throwable e, ErrorCode code, Object... args) {
        super(code.getCode(), MessageFormat.format(code.getMsg(), args), e);
    }
}
