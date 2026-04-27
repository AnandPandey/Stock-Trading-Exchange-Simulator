package StockTradingExchangeSimulator;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe statistics tracker for the exchange.
 *
 * Strategy:
 * - totalTrades and totalVolume: AtomicInteger/Long (independent, simple updates)
 * - Price stats: ReadWriteLock (related values read together for consistency)
 */
public class ExchangeStats {
    private final AtomicInteger totalTrades = new AtomicInteger(0);
    private final AtomicLong totalVolume = new AtomicLong(0);

    // ReadWriteLock ensures consistent price snapshots
    private final ReadWriteLock priceLock = new ReentrantReadWriteLock();
    private double lastTradePrice = 0.0;
    private double highestPrice = 0.0;
    private double lowestPrice = Double.MAX_VALUE;

    /**
     * Record a trade execution. Called by matching engine.
     * Atomically updates independent counts; updates price with write lock.
     */
    public void recordTrade(double price, int quantity) {
        // Atomic operations - no lock needed
        totalTrades.incrementAndGet();
        totalVolume.addAndGet(quantity);

        // Price updates - need write lock for consistency
        priceLock.writeLock().lock();
        try {
            lastTradePrice = price;
            if (price > highestPrice) highestPrice = price;
            if (price < lowestPrice) lowestPrice = price;
        } finally {
            priceLock.writeLock().unlock();
        }
    }

    /**
     * Get a consistent snapshot of all statistics.
     * Reads are done with read lock to ensure prices are from same moment.
     */
    public String getSnapshot() {
        priceLock.readLock().lock();
        try {
            return String.format(
                    "Trades: %d | Volume: %d | Last: %.2f | High: %.2f | Low: %.2f",
                    totalTrades.get(), totalVolume.get(),
                    lastTradePrice, highestPrice, lowestPrice);
        } finally {
            priceLock.readLock().unlock();
        }
    }

    /**
     * Get total number of executed trades (atomic read).
     */
    public int getTotalTrades() {
        return totalTrades.get();
    }

    /**
     * Get total volume traded (atomic read).
     */
    public long getTotalVolume() {
        return totalVolume.get();
    }
}
