package com.github.bannirui.msb.common.id;

import com.github.bannirui.msb.common.ex.FrameworkException;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(EasyGenerator.class);

    private final CircleArray circleArray;
    private final int nodeId;
    private final long beginTime;

    public EasyGenerator(int nodeId, int timeWait) {
        this.nodeId = nodeId;
        this.circleArray = new CircleArray(timeWait);
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 3, 8, 0, 0, 0);
        this.beginTime = calendar.getTime().getTime();
        logger.info("EasyGenerator initialized, worker id is {}", this.nodeId);
    }

    public IdResult generateIdResult() {
        while (true) {
            long timestamp = System.currentTimeMillis() - this.beginTime;
            // second
            timestamp /= 1_000L;
            long sequence = this.circleArray.generateSequence(timestamp);
            if (sequence < 0xffffe) {
                return new IdResult(timestamp, sequence, (long) this.nodeId);
            }
            try {
                Thread.sleep(100L);
            } catch (Exception e) {
                throw FrameworkException.getInstance(e, "generate id err", new Object[0]);
            }
        }
    }

    public long newId(){
        return this.generateIdResult().generateId();
    }
}
