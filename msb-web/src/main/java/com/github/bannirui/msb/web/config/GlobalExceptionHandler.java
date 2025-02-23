package com.github.bannirui.msb.web.config;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

    @ResponseBody
    @ExceptionHandler({Exception.class})
    public Result<Void> exceptionHandler(Exception e) {
        Result<Void> result = new Result();
        result.setStatus(false);
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException)e;
            result.setStatusCode(baseException.getCode());
            result.setMessage(baseException.getMessage());
        } else if (e instanceof AccessDeniedException) {
            result.setStatusCode("UN_AUTH");
            result.setMessage("您无权限访问此资源");
        } else if (e instanceof MissingServletRequestParameterException) {
            result.setStatusCode("MISSING_PARAMETER");
            result.setMessage("访问参数异常");
        } else {
            if (e instanceof BindException) {
                StringBuilder strBuilder = new StringBuilder();
                BindException bindException = (BindException)e;
                BindingResult bindingResult = bindException.getBindingResult();
                List<ObjectError> list = bindingResult.getAllErrors();
                Iterator var14 = list.iterator();

                while(var14.hasNext()) {
                    ObjectError error = (ObjectError)var14.next();
                    strBuilder.append(error.getDefaultMessage() + "\n");
                }

                result.setStatusCode("PARAM_ERROR");
                result.setMessage(strBuilder.toString());
                return result;
            }

            if (e instanceof ConstraintViolationException) {
                ConstraintViolationException constraintViolationException = (ConstraintViolationException)e;
                Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
                StringBuilder strBuilder = new StringBuilder();
                Iterator var6 = violations.iterator();

                while(var6.hasNext()) {
                    ConstraintViolation<?> violation = (ConstraintViolation)var6.next();
                    strBuilder.append(violation.getMessage() + "\n");
                }

                result.setStatusCode("PARAM_ERROR");
                result.setMessage(strBuilder.toString());
                return result;
            }

            if (e instanceof ServletRequestBindingException) {
                result.setStatusCode("PARAM_ERROR");
                result.setMessage("请求参数绑定异常" + e.getMessage());
            } else if (e instanceof IllegalArgumentException) {
                result.setStatusCode("PARAM_ERROR");
                result.setMessage("非法参数异常" + e.getMessage());
            } else if (e instanceof UnknownHostException) {
                result.setStatusCode("UNKNOWN_HOST");
                result.setMessage("未知主机" + e.getMessage());
            } else if (e instanceof HttpMessageNotReadableException) {
                result.setStatusCode("PARAM_ERROR");
                result.setMessage("请求参数异常:" + e.getMessage());
            } else if (e instanceof HttpMediaTypeNotSupportedException) {
                result.setStatusCode("PARAM_ERROR");
                result.setMessage("请求类型错误:" + ((HttpMediaTypeNotSupportedException)e).getContentType() + " 不支持");
            } else if (e instanceof MethodArgumentNotValidException) {
                result.setStatusCode("PARAM_ERROR");
                result.setMessage(e.getMessage());
            } else {
                result.setStatusCode("DEF001");
                result.setMessage("未知异常，请联系系统管理员");
                logger.error("未知异常", e);
            }
        }

        return result;
    }
}
