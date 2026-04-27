package multithreading_refactor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SharedBuffer {

    private final Queue<Integer> buffer = new LinkedList<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    void produce(int value) {
        lock.lock();
        try {
            int capacity = 1;
            while (buffer.size() == capacity) {
                notFull.await();
            }

            buffer.add(value);
            System.out.println("Produced: " + value);

            notEmpty.signal();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    void consume() {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                notEmpty.await();
            }
            int value = buffer.poll();
            System.out.println("Consumed: " + value);

            notFull.signal();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}