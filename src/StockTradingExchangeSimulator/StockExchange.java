package StockTradingExchangeSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stock Trading Exchange Simulator - Main Entry Point
 *
 * Wires together all components with proper thread pool lifecycle:
 * 1. Fixed thread pool for traders (5 traders, 3 threads)
 * 2. Single thread executor for matching engine
 * 3. Scheduled executor for dashboard (managed internally)
 * 4. Graceful shutdown with Future.get() to collect results
 */
public class StockExchange {
    public static void main(String[] args) throws Exception {
        // ==================== INITIALIZATION ====================

        // Shared components
        OrderBook orderBook = new OrderBook(100);
        ExchangeStats stats = new ExchangeStats();
        AtomicBoolean exchangeOpen = new AtomicBoolean(true);
        AtomicInteger orderIdCounter = new AtomicInteger(0);

        // Thread pools
        ExecutorService traderPool = Executors.newFixedThreadPool(3);
        ExecutorService enginePool = Executors.newSingleThreadExecutor();

        // Dashboard (manages its own scheduler)
        Dashboard dashboard = new Dashboard(stats, orderBook);
        dashboard.start();

        System.out.println("\n===== EXCHANGE STARTING =====");
        System.out.println("Traders: 5 | Thread Pool: 3 | Duration: 15 seconds");
        System.out.println("=============================\n");

        // ==================== SUBMIT TRADERS ====================

        List<Future<String>> traderFutures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Trader trader = new Trader("Trader-" + i, orderBook,
                    orderIdCounter, exchangeOpen);
            Future<String> future = traderPool.submit(trader);
            traderFutures.add(future);
        }

        // ==================== SUBMIT ENGINE ====================

        MatchingEngine engine = new MatchingEngine(orderBook, stats,
                exchangeOpen);
        Future<String> engineFuture = enginePool.submit(engine);

        // ==================== RUN FOR 15 SECONDS ====================

        Thread.sleep(15000);

        // ==================== INITIATE SHUTDOWN ====================

        System.out.println("\n===== INITIATING SHUTDOWN =====");
        exchangeOpen.set(false);      // Signal all threads to stop
        orderBook.wakeAll();          // Unblock any waiting threads

        // Shutdown pools (no new tasks accepted)
        traderPool.shutdown();
        enginePool.shutdown();

        // ==================== COLLECT RESULTS ====================

        System.out.println("\n===== EXCHANGE CLOSING =====");
        try {
            // Collect trader results
            for (int i = 0; i < traderFutures.size(); i++) {
                String result = traderFutures.get(i)
                        .get(10, TimeUnit.SECONDS);
                System.out.println(result);
            }

            // Collect engine result
            String engineResult = engineFuture
                    .get(10, TimeUnit.SECONDS);
            System.out.println(engineResult);

        } catch (TimeoutException e) {
            System.err.println("ERROR: Timeout waiting for tasks to complete");
            System.err.println("Forcing shutdown of pools...");
            traderPool.shutdownNow();
            enginePool.shutdownNow();
        }

        // ==================== WAIT FOR TERMINATION ====================

        // Wait for graceful termination
        if (!traderPool.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Trader pool did not terminate, forcing...");
            traderPool.shutdownNow();
        }

        if (!enginePool.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Engine pool did not terminate, forcing...");
            enginePool.shutdownNow();
        }

        // ==================== FINAL SUMMARY ====================

        dashboard.stop();

        System.out.println("\n===== FINAL SUMMARY =====");
        System.out.println(stats.getSnapshot());
        System.out.println("Total orders placed: " + orderIdCounter.get());
        System.out.println("Unmatched orders: " + orderBook.getOrderCount() +
                " (" + orderBook.getBuyCount() + " buy, " +
                orderBook.getSellCount() + " sell)");
        System.out.println("=========================\n");
        System.out.println("All pools shut down successfully.");
    }
}