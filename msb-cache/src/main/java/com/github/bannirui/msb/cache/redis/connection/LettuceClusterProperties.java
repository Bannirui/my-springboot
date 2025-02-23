package com.github.bannirui.msb.cache.redis.connection;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "spring.redis.lettuce.cluster"
)
public class LettuceClusterProperties {
    private LettuceClusterProperties.Refresh refresh = new LettuceClusterProperties.Refresh();
    private Duration commandTimeout = Duration.ofSeconds(10L);

    public LettuceClusterProperties.Refresh getRefresh() {
        return this.refresh;
    }

    public void setRefresh(LettuceClusterProperties.Refresh refresh) {
        this.refresh = refresh;
    }

    public Duration getCommandTimeout() {
        return this.commandTimeout;
    }

    public void setCommandTimeout(Duration commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public static class Refresh {
        private Duration period = Duration.ofSeconds(30L);
        private boolean adaptive = true;

        public Refresh() {
        }

        public Duration getPeriod() {
            return this.period;
        }

        public void setPeriod(Duration period) {
            this.period = period;
        }

        public boolean isAdaptive() {
            return this.adaptive;
        }

        public void setAdaptive(boolean adaptive) {
            this.adaptive = adaptive;
        }
    }
}
