package com.github.bannirui.msb.config.aop;

import com.github.bannirui.msb.common.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.config.processor.FlowControlProcessor;
import com.github.bannirui.msb.common.util.StringUtil;
import com.github.bannirui.msb.config.annotation.FlowControlSwitch;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ReflectionUtils;

@Aspect
public class FlowControlAop {
    private static Map<String, FlowControlProcessor> instance = new ConcurrentHashMap();
    private static final String APOLLO_WEIGHT_RATIO_KEY = "config.flowControl.weightRatio";

    public FlowControlAop() {
    }

    @Around("@annotation(flowControlSwitch)")
    public Object executeControl(ProceedingJoinPoint jp, FlowControlSwitch flowControlSwitch) throws Throwable {
        FlowControlProcessor flowControlProcessor = null;
        if (instance.containsKey(flowControlSwitch.processor().getName())) {
            flowControlProcessor = (FlowControlProcessor) instance.get(flowControlSwitch.processor().getName());
        } else {
            flowControlProcessor = (FlowControlProcessor) flowControlSwitch.processor().newInstance();
            instance.put(flowControlSwitch.processor().getName(), flowControlProcessor);
        }

        String weightRatio = StringUtil.isNotEmpty(MsbEnvironmentMgr.getProperty("config.flowControl.weightRatio")) ?
            MsbEnvironmentMgr.getProperty("config.flowControl.weightRatio") : String.valueOf(flowControlSwitch.weightRatio());
        if (flowControlProcessor.switchOff(Integer.valueOf(weightRatio), jp.getArgs())) {
            return jp.proceed(jp.getArgs());
        } else {
            Method method = MethodManager.findMethod(jp.getTarget().getClass(), flowControlSwitch.fallback(),
                ((MethodSignature) jp.getSignature()).getMethod().getParameterTypes());
            return ReflectionUtils.invokeMethod(method, jp.getTarget(), jp.getArgs());
        }
    }
}
