package com.github.bannirui.msb.web.session;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "session.redis.lettuce.cluster"
)
public class SessionLettuceClusterProperties {
    public static final String PREFIX = "titans.session.redis.lettuce.cluster";
    private SessionLettuceClusterProperties.Refresh refresh = new SessionLettuceClusterProperties.Refresh();
    private Duration commandTimeout = Duration.ofSeconds(10L);

    public SessionLettuceClusterProperties.Refresh getRefresh() {
        return this.refresh;
    }

    public void setRefresh(SessionLettuceClusterProperties.Refresh refresh) {
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
