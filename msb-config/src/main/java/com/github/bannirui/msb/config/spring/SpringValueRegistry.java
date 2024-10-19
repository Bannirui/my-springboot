package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

public class SpringValueRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SpringValueRegistry.class);
    private static final long CLEAN_INTERVAL_IN_SECONDS = 5L;
    private final Map<BeanFactory, Multimap<String, SpringValue>> registry = Maps.newConcurrentMap();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object LOCK = new Object();

    public SpringValueRegistry() {
    }

    public void register(BeanFactory beanFactory, String key, SpringValue springValue) {
        if (!this.registry.containsKey(beanFactory)) {
            synchronized (this.LOCK) {
                if (!this.registry.containsKey(beanFactory)) {
                    this.registry.put(beanFactory, Multimaps.synchronizedListMultimap(LinkedListMultimap.create()));
                }
            }
        }

        ((Multimap) this.registry.get(beanFactory)).put(key, springValue);
        if (this.initialized.compareAndSet(false, true)) {
            this.initialize();
        }

    }

    public Collection<SpringValue> get(BeanFactory beanFactory, String key) {
        Multimap<String, SpringValue> beanFactorySpringValues = (Multimap) this.registry.get(beanFactory);
        return beanFactorySpringValues == null ? null : beanFactorySpringValues.get(key);
    }

    private void initialize() {
        Executors.newSingleThreadScheduledExecutor(ApolloThreadFactory.create("SpringValueRegistry", true)).scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    SpringValueRegistry.this.scanAndClean();
                } catch (Throwable e) {
                    SpringValueRegistry.logger.error(e.getMessage(), e);
                }

            }
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    private void scanAndClean() {
        Iterator<Multimap<String, SpringValue>> iterator = this.registry.values().iterator();
        while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
            Multimap<String, SpringValue> springValues = (Multimap) iterator.next();
            Iterator<Map.Entry<String, SpringValue>> springValueIterator = springValues.entries().iterator();
            while (springValueIterator.hasNext()) {
                Map.Entry<String, SpringValue> springValue = (Map.Entry) springValueIterator.next();
                if (!((SpringValue) springValue.getValue()).isTargetBeanValid()) {
                    springValueIterator.remove();
                }
            }
        }
    }
}
