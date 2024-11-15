package com.github.bannirui.msb.log.queue;

import java.util.Objects;
import java.util.function.BiFunction;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;
import org.jctools.util.UnsafeRefArrayAccess;

public class MpscArrayQueue4Log<E> extends MpscArrayQueue<E> {
    public MpscArrayQueue4Log(int capacity) {
        super(capacity);
    }

    public boolean offerIfBelowThreshold(E e, int threshold, BiFunction<Long, E, Boolean> refuseOffer, MessagePassingQueue.Consumer<E> beforeAdd) {
        if (Objects.isNull(e)) {
            throw new NullPointerException();
        }
        long mask = super.mask;
        long capacity = mask + 1L;
        long producerLimit = super.lvProducerLimit();
        long pIndex;
        long offset;
        do {
            pIndex = super.lvProducerIndex();
            offset = producerLimit - pIndex;
            long size = capacity - offset;
            if (size >= threshold) {
                long cIndex = super.lvConsumerIndex();
                size = pIndex - cIndex;
                if (size >= capacity) {
                    return false;
                }
                if (size >= threshold && refuseOffer.apply(size, e)) {
                    return false;
                }
                producerLimit = cIndex + capacity;
                super.soProducerLimit(producerLimit);
            }
        } while (!super.casProducerIndex(pIndex, pIndex + 1));
        offset = calcElementOffset(pIndex, mask);
        beforeAdd.accept(e);
        UnsafeRefArrayAccess.soElement(super.buffer, offset, e);
        return true;
    }

    public E take(InterruptedWaitStrategy w, InterruptedExitCondition exit) throws InterruptedException {
        E[] buffer = super.buffer;
        long mask = super.mask;
        long cIndex = super.lpConsumerIndex();
        for (int counter = 0; exit.keepRunning(); counter = w.idle(counter)) {
            long offset = calcElementOffset(cIndex, mask);
            E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
            if (Objects.nonNull(e)) {
                ++cIndex;
                UnsafeRefArrayAccess.spElement(buffer, offset, null);
                super.soConsumerIndex(cIndex);
                return e;
            }
        }
        return null;
    }

    interface InterruptedWaitStrategy {
        int idle(int idleCounter) throws InterruptedException;
    }

    interface InterruptedExitCondition {
        boolean keepRunning() throws InterruptedException;
    }
}
