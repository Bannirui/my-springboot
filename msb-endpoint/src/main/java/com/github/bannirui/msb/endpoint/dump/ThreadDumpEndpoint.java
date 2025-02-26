package com.github.bannirui.msb.endpoint.dump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;

public class ThreadDumpEndpoint {

    public ThreadDumpEndpoint.ThreadDumpDescriptor threadDump() {
        return new ThreadDumpEndpoint.ThreadDumpDescriptor(Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)));
    }

    public static final class ThreadDumpDescriptor {
        private final List<ThreadInfo> threads;

        private ThreadDumpDescriptor(List<ThreadInfo> threads) {
            this.threads = threads;
        }

        public List<ThreadInfo> getThreads() {
            return this.threads;
        }
    }
}
