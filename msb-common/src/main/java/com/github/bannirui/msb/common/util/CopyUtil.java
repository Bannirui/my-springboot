package com.github.bannirui.msb.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

public class CopyUtil {

    public static final Logger log = LoggerFactory.getLogger(CopyUtil.class);

    public CopyUtil() {
    }

    public static <T> T copy(Object from, T infoTo) {
        if (from == null) {
            return null;
        } else {
            try {
                BeanUtils.copyProperties(from, infoTo);
                return infoTo;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <T> T copy(Object from, Class<T> clazz) {
        if (from == null) {
            return null;
        } else {
            try {
                T infoTo = clazz.newInstance();
                BeanUtils.copyProperties(from, infoTo);
                return infoTo;
            } catch (IllegalAccessException | InstantiationException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <S, T> List<T> copy(List<S> from, Class<T> toClazz) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        } else {
            List<T> result = new ArrayList<>(from.size());
            for (S src : from) {
                try {
                    T infoTo = toClazz.newInstance();
                    BeanUtils.copyProperties(src, infoTo);
                    result.add(infoTo);
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error(e.getMessage(), e);
                }
            }

            return result;
        }
    }
}
