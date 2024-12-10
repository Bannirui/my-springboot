package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * 仿写的{@link com.ctrip.framework.apollo.spring.util.SpringInjector}.
 */
public class SpringInjector {

    private static volatile Injector s_injector;
    private static final Object lock = new Object();

    public SpringInjector() {
    }

    /**
     * DCL单例.
     */
    private static Injector getInjector() {
        if (s_injector == null) {
            synchronized (lock) {
                if (s_injector == null) {
                    try {
                        s_injector = Guice.createInjector(new SpringModule());
                    } catch (Throwable e) {
                        ApolloConfigException ex = new ApolloConfigException("Failed to initialize Spring Injector", e);
                        Tracer.logError(ex);
                        throw e;
                    }
                }
            }
        }
        return s_injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable e) {
            Tracer.logError(e);
            throw new ApolloConfigException(String.format("Unable to load instance for %s!", clazz.getName()));
        }
    }

    private static class SpringModule extends AbstractModule {
        private SpringModule() {
        }

        @Override
        protected void configure() {
            // 显式指定单例进行DI
            super.bind(PlaceholderHelper.class).in(Singleton.class);
            super.bind(SpringValueRegistry.class).in(Singleton.class);
            super.bind(ConfigPropertySourceFactory.class).in(Singleton.class);
        }
    }
}
