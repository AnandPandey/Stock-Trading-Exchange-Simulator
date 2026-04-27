package StockTradingExchangeSimulator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dashboard that periodically prints live exchange statistics.
 * Uses ScheduledExecutorService instead of Thread.sleep() loops.
 */
public class Dashboard {
    private final ExchangeStats stats;
    private final OrderBook orderBook;
    private final ScheduledExecutorService scheduler;

    /**
     * Create a dashboard.
     * @param stats The exchange statistics tracker
     * @param orderBook The order book
     */
    public Dashboard(ExchangeStats stats, OrderBook orderBook) {
        this.stats = stats;
        this.orderBook = orderBook;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Start the dashboard - prints stats every 2 seconds.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n===== EXCHANGE DASHBOARD =====");
            System.out.println(stats.getSnapshot());
            System.out.println("Pending orders: " +
                    orderBook.getOrderCount() +
                    " (" + orderBook.getBuyCount() + " buy, " +
                    orderBook.getSellCount() + " sell)");
            System.out.println("=============================");
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Stop the dashboard gracefully.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}