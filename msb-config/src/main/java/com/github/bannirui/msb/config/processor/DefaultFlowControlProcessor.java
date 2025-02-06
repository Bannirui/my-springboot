package com.github.bannirui.msb.config.processor;

import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFlowControlProcessor implements FlowControlProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFlowControlProcessor.class);
    private static final int MAX_WEIGHT = 100;
    private static final int MIN_WEIGHT = 0;

    public DefaultFlowControlProcessor() {
    }

    @Override
    public boolean switchOff(int weight, Object... args) {
        if (weight > 100 && weight < 0) {
            throw new ErrorCodeException(ExceptionEnum.CONFIG_WEIGHT_RATIO_ERROR, new Object[0]);
        } else {
            int remainingWeightRatio = 100 - weight;
            List<Integer> targetValueRange = null;
            List<Integer> fallbackValueRance = null;
            if (remainingWeightRatio < weight) {
                targetValueRange = this.fillRangeValue(remainingWeightRatio);
                fallbackValueRance = this.fillRangeValue(remainingWeightRatio + 1, 100);
            } else if (remainingWeightRatio > weight) {
                fallbackValueRance = this.fillRangeValue(weight);
                targetValueRange = this.fillRangeValue(weight + 1, 100);
            } else {
                targetValueRange = this.fillRangeValue(remainingWeightRatio);
                fallbackValueRance = this.fillRangeValue(51, 100);
            }

            int random = getRandom(1, 100);
            if (targetValueRange.contains(random)) {
                return true;
            } else if (fallbackValueRance.contains(random)) {
                return false;
            } else {
                logger.error("DefaultFlowControlProcessor.switchOff算法错误，默认返回true");
                return true;
            }
        }
    }

    private List<Integer> fillRangeValue(int weight) {
        List<Integer> temp = new ArrayList();
        for(int i = 1; i <= weight; ++i) {
            temp.add(i);
        }
        return temp;
    }

    private List<Integer> fillRangeValue(int weight, int maxWeight) {
        List<Integer> temp = new ArrayList();
        for(int i = weight; i <= maxWeight; ++i) {
            temp.add(i);
        }
        return temp;
    }

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
}
