package StockTradingExchangeSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe OrderBook using ReentrantLock and Condition variables.
 *
 * Two Condition variables prevent unnecessary wakeups:
 * - ordersAvailable: Matching engine waits when no match exists
 * - bookNotFull:     Traders wait when book is at capacity
 */
public class OrderBook {
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();
    private final int capacity;

    private final ReentrantLock lock = new ReentrantLock(true); // fair!
    private final Condition ordersAvailable = lock.newCondition();
    private final Condition bookNotFull = lock.newCondition();

    /**
     * Create an order book with given capacity.
     * @param capacity Maximum number of pending orders (buy + sell)
     */
    public OrderBook(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Add an order to the book. Blocks if book is full.
     * Signals ordersAvailable to wake matching engine.
     */
    public void addOrder(Order order) throws InterruptedException {
        lock.lock();
        try {
            // Wait while book is full
            while (buyOrders.size() + sellOrders.size() >= capacity) {
                bookNotFull.await();
            }

            // Add to appropriate list
            if (order.getType() == Order.OrderType.BUY) {
                buyOrders.add(order);
            } else {
                sellOrders.add(order);
            }

            // Wake matching engine
            ordersAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Extract a matched pair of orders. Blocks if no match exists.
     * Must be called with exchangeOpen parameter to check shutdown flag.
     *
     * @param exchangeOpen AtomicBoolean flag indicating exchange is running
     * @return MatchedPair if found, null if exchange closed and no match
     */
    public MatchedPair takeBestMatch(AtomicBoolean exchangeOpen)
            throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                // Try to find a match
                MatchedPair match = findBestMatch();
                if (match != null) {
                    // Found match - remove orders and signal traders
                    buyOrders.remove(match.getBuyOrder());
                    sellOrders.remove(match.getSellOrder());
                    bookNotFull.signalAll();
                    return match;
                }

                // No match found - check if exchange is shutting down
                if (!exchangeOpen.get()) {
                    return null;
                }

                // Exchange still open but no match - wait with timeout
                // Timeout allows periodic check of shutdown flag
                ordersAvailable.await(200, TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Find the best (first available) matchable pair.
     * Only called while holding lock.
     *
     * @return MatchedPair if any buy and sell match, null otherwise
     */
    private MatchedPair findBestMatch() {
        for (Order buy : buyOrders) {
            for (Order sell : sellOrders) {
                if (buy.getStock().equals(sell.getStock()) &&
                        buy.getPrice() >= sell.getPrice()) {
                    return new MatchedPair(buy, sell);
                }
            }
        }
        return null;
    }

    /**
     * Get total pending orders in book.
     */
    public int getOrderCount() {
        lock.lock();
        try {
            return buyOrders.size() + sellOrders.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get count of pending buy orders.
     */
    public int getBuyCount() {
        lock.lock();
        try {
            return buyOrders.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get count of pending sell orders.
     */
    public int getSellCount() {
        lock.lock();
        try {
            return sellOrders.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wake all waiting threads on both conditions.
     * Called during shutdown to unblock any threads awaiting on conditions.
     */
    public void wakeAll() {
        lock.lock();
        try {
            ordersAvailable.signalAll();
            bookNotFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
}