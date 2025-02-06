package com.github.bannirui.msb.id;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircleArray {

    private Logger logger = LoggerFactory.getLogger(CircleArray.class);

    private final int capacity;
    private AtomicReferenceArray<IdSeed> idSeeds;

    public CircleArray(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity must greater than 2");
        }
        this.capacity = capacity;
        this.idSeeds = new AtomicReferenceArray<>(capacity);
    }

    public long generateSequence(long timeStamp) {
        int ix = (int) (timeStamp % (long) this.capacity);
        IdSeed seed = this.idSeeds.get(ix);
        if(Objects.nonNull(seed) && seed.getTimeStamp()==timeStamp) return seed.increment();
        IdSeed newSeed=new IdSeed(timeStamp);
        this.idSeeds.compareAndSet(ix,seed,newSeed);
        try{
            Thread.sleep(100L);
        }catch (InterruptedException e){
            this.logger.error(e.getMessage(), e);
        }
        return this.idSeeds.get(ix).increment();
    }
}
