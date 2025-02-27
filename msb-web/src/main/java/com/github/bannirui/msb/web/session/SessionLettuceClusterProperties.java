package com.github.bannirui.msb.web.session;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(
    prefix = SessionLettuceClusterProperties.PREFIX
)
public class SessionLettuceClusterProperties {
    public static final String PREFIX = "msb.session.redis.lettuce.cluster";
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
