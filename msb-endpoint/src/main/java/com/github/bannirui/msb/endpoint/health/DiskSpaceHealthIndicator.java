package com.github.bannirui.msb.endpoint.health;

import com.github.bannirui.msb.util.AssertUtil;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskSpaceHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);
    private static final int MEGABYTES = 1048576;
    private static final int DEFAULT_THRESHOLD = 10485760;
    private File path = new File(".");
    private long threshold = 10485760L;

    public long getThreshold() {
        return this.threshold;
    }

    public void setThreshold(long threshold) {
        AssertUtil.isTrue(threshold >= 0L, "磁盘空间阈值必须大于0");
        this.threshold = threshold;
    }

    @Override
    public Health health() {
        Health health = new Health();
        long diskFreeInBytes = this.path.getUsableSpace();
        if (diskFreeInBytes >= this.getThreshold()) {
            health.up();
        } else {
            logger.warn("Free disk space below threshold. Available: {} bytes (threshold: {} bytes", diskFreeInBytes, this.getThreshold());
            health.down();
        }
        health.withDetail("total", this.path.getTotalSpace() / 1024L / 1024L / 1024L + "G").withDetail("free", diskFreeInBytes / 1024L / 1024L / 1024L + "G").withDetail("threshold", this.getThreshold() / 1024L / 1024L + "M");
        return health;
    }
}
