package com.github.bannirui.msb.log.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.github.bannirui.msb.common.ex.BusinessException;

public class CatUtil {

    public static void processException(Exception exception, Transaction transaction) {
        if (BusinessException.isBusinessException(exception)) {
            if (transaction != null) {
                transaction.setStatus("0");
            }
        } else if (transaction != null) {
            Cat.logError(exception);
            transaction.setStatus(exception.getClass().getSimpleName());
        }
    }
}
