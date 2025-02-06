package com.github.bannirui.msb.orm.aop;

import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.orm.annotation.MultiTransactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Aspect
public class MultiTransactionalInterceptor implements ApplicationContextAware {
    private Logger log = LoggerFactory.getLogger(MultiTransactionalInterceptor.class);
    private ApplicationContext applicationContext;

    @Around("@annotation(multiTransactional)")
    public Object interceptor(ProceedingJoinPoint jp, MultiTransactional multiTransactional) throws Throwable {
        Object proceed = null;
        String[] value = multiTransactional.value();
        if (ArrayUtils.isEmpty(value)) {
            DataSourceTransactionManager dataSourceTransactionManager = null;
            try {
                dataSourceTransactionManager = this.applicationContext.getBean(DataSourceTransactionManager.class);
            } catch (NoUniqueBeanDefinitionException e) {
                throw new ErrorCodeException(ExceptionEnum.ORM_TRANSACTION_ERROR, new Object[0]);
            }
            TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(0);
            TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
            try {
                proceed = jp.proceed(jp.getArgs());
                dataSourceTransactionManager.commit(transactionStatus);
            } catch (Exception e) {
                dataSourceTransactionManager.rollback(transactionStatus);
                throw e;
            }
        } else {
            List<DataSourceTransactionManager> dataSourceTransactionManagerList = new ArrayList<>(value.length);
            for (String configName : value) {
                DataSourceTransactionManager dataSourceTransactionManager = null;
                try {
                    dataSourceTransactionManager = (DataSourceTransactionManager)this.applicationContext.getBean(configName + "TransactionManager");
                } catch (NoSuchBeanDefinitionException e) {
                    throw new ErrorCodeException(ExceptionEnum.ORM_CONFIG_NAME_ERROR, new Object[0]);
                }
                dataSourceTransactionManagerList.add(dataSourceTransactionManager);
            }
            List<TransactionStatus> statuses = new ArrayList<>();
            try {
                for (DataSourceTransactionManager dataSourceTransactionManager : dataSourceTransactionManagerList) {
                    TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(0);
                    TransactionStatus transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    statuses.add(transaction);
                }
            } catch (Exception e) {
                for(int i = statuses.size() - 1; i >=0; --i) {
                    try {
                        dataSourceTransactionManagerList.get(i).rollback(statuses.get(i));
                    } catch (Exception ex) {
                        this.log.error("反顺序回滚已开启事务异常", ex);
                    }
                }
                throw e;
            }
            try {
                proceed = jp.proceed(jp.getArgs());
                for(int i = dataSourceTransactionManagerList.size() - 1; i >=0; --i) {
                    dataSourceTransactionManagerList.get(i).commit(statuses.get(i));
                }
            } catch (Exception e) {
                for(int i = dataSourceTransactionManagerList.size() - 1; i >=0; --i) {
                    try {
                        dataSourceTransactionManagerList.get(i).rollback(statuses.get(i));
                    } catch (Exception ex) {
                        this.log.error("反顺序回滚事务异常", ex);
                    }
                }
                throw e;
            }
        }
        return proceed;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
