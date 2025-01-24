package com.github.bannirui.msb.mq.sdk.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListUtil {
    private static final Logger logger = LoggerFactory.getLogger(ListUtil.class);

    public static <T> List<List<T>> subList(List<T> tList, Integer subNum) {
        List<List<T>> tNewList = new ArrayList<>();
        int totalNum = tList.size();
        int insertTimes = totalNum / subNum;
        for(int i = 0; i <= insertTimes; ++i) {
            int priIndex = subNum * i;
            int lastIndex = priIndex + subNum;
            List<T> subNewList;
            if (i == insertTimes) {
                if (logger.isDebugEnabled()) {
                    logger.debug("最后一次截取：" + priIndex + "," + lastIndex);
                }
                subNewList = tList.subList(priIndex, tList.size());
            } else {
                subNewList = tList.subList(priIndex, lastIndex);
            }
            if (CollectionUtils.isNotEmpty(subNewList)) {
                tNewList.add(subNewList);
            }
        }
        return tNewList;
    }
}
