package com.github.bannirui.msb.log.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import org.jctools.queues.MessagePassingQueue;

public class MpscBlockingQueue4Log<E> implements BlockingQueue<E> {

    private MpscArrayQueue4Log<E> logQueue = null;

    public MpscBlockingQueue4Log(int capacity) {
        this.logQueue = new MpscArrayQueue4Log<>(capacity);
    }

    @Override
    public boolean add(E e) {
        return this.logQueue.add(e);
    }

    @Override
    public boolean offer(E e) {
        return this.logQueue.offer(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
        this.logQueue.offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return this.logQueue.offer(e);
    }

    @Override
    public E take() throws InterruptedException {
        return this.logQueue.take((idleCounter -> {
            Thread.sleep(0L);
            return idleCounter++;
        }), () -> {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            } else {
                return true;
            }
        });
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.logQueue.poll();
    }

    @Override
    public int remainingCapacity() {
        return this.logQueue.capacity() - this.logQueue.size();
    }

    @Override
    public boolean remove(Object o) {
        return this.logQueue.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return this.logQueue.contains(o);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            return 0;
        } else {
            int sz = this.logQueue.size();
            for (int i = 0; i < sz; i++) {
                E result = this.logQueue.poll();
                if (result != null) {
                    c.add(result);
                }
            }
            return c.size();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            return 0;
        } else {
            int sz = this.logQueue.size();
            for (int i = 0; i < sz && i + 1 < maxElements; i++) {
                E result = this.logQueue.poll();
                if (result != null) {
                    c.add(result);
                }
            }
            return c.size();
        }
    }

    @Override
    public E remove() {
        return this.logQueue.remove();
    }

    @Override
    public E poll() {
        return this.logQueue.poll();
    }

    @Override
    public E element() {
        return this.logQueue.element();
    }

    @Override
    public E peek() {
        return this.logQueue.peek();
    }

    @Override
    public int size() {
        return this.logQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.logQueue.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        int sz = this.logQueue.size();
        List<E> lst = new LinkedList<E>();
        for (int i = 0; i < sz; i++) {
            E result = this.logQueue.poll();
            if (result != null) {
                lst.add(result);
            }
        }
        return lst.iterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.logQueue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.logQueue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.logQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.logQueue.retainAll(c);
    }

    @Override
    public void clear() {
        this.logQueue.clear();
    }

    public boolean offerIfBelowThreshold(E e, int threshold, BiFunction<Long, E, Boolean> refuseOffer, MessagePassingQueue.Consumer<E> beforeAdd) {
        return this.logQueue.offerIfBelowThreshold(e, threshold, refuseOffer, beforeAdd);
    }
}
