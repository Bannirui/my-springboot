package com.dianping.cat;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;
import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

public class CatClientModule extends AbstractModule {

    public static final String ID = "cat-client";

    protected void execute(ModuleContext ctx) throws Exception {
        ctx.info("Current working directory is " + System.getProperty("user.dir"));
        MilliSecondTimer.initialize();
        Threads.addListener(new CatClientModule.CatThreadListener(ctx));
        Cat.getInstance().setContainer(((DefaultModuleContext) ctx).getContainer());
        ctx.lookup(TransportManager.class);
        ClientConfigManager clientConfigManager = ctx.lookup(ClientConfigManager.class);
        if (clientConfigManager.isCatEnabled()) {
            StatusUpdateTask statusUpdateTask = (StatusUpdateTask) ctx.lookup(StatusUpdateTask.class);
            Threads.forGroup("cat").start(statusUpdateTask);
            LockSupport.parkNanos(10_000_000L);
        }
    }

    public Module[] getDependencies(ModuleContext ctx) {
        return null;
    }

    public static final class CatThreadListener extends Threads.AbstractThreadListener {
        private final ModuleContext m_ctx;

        private CatThreadListener(ModuleContext ctx) {
            this.m_ctx = ctx;
        }

        public void onThreadGroupCreated(ThreadGroup group, String name) {
            this.m_ctx.info(String.format("Thread group(%s) created.", name));
        }

        public void onThreadPoolCreated(ExecutorService pool, String name) {
            this.m_ctx.info(String.format("Thread pool(%s) created.", name));
        }

        public void onThreadStarting(Thread thread, String name) {
            this.m_ctx.info(String.format("Starting thread(%s) ...", name));
        }

        public void onThreadStopping(Thread thread, String name) {
            this.m_ctx.info(String.format("Stopping thread(%s).", name));
        }

        public boolean onUncaughtException(Thread thread, Throwable e) {
            this.m_ctx.error(String.format("Uncaught exception thrown out of thread(%s)", thread.getName()), e);
            return true;
        }
    }
}
