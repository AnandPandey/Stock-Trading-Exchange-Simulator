package StockTradingExchangeSimulator;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Matching engine that continuously extracts matched order pairs from the
 * OrderBook and records their execution in ExchangeStats.
 *
 * This is the consumer in the producer-consumer pattern (traders are producers).
 * Implements Callable<String> to return completion summary via Future.
 */
public class MatchingEngine implements Callable<String> {
    private final OrderBook orderBook;
    private final ExchangeStats stats;
    private final AtomicBoolean exchangeOpen;

    /**
     * Create a new matching engine.
     * @param orderBook The shared order book to pull matches from
     * @param stats The exchange statistics tracker
     * @param exchangeOpen Flag indicating exchange is running
     */
    public MatchingEngine(OrderBook orderBook, ExchangeStats stats,
                          AtomicBoolean exchangeOpen) {
        this.orderBook = orderBook;
        this.stats = stats;
        this.exchangeOpen = exchangeOpen;
    }

    @Override
    public String call() throws Exception {
        int matchedTrades = 0;
        System.out.println("[Engine] Started.");

        while (true) {
            // Pull next match from order book
            // Returns null when exchange is closed and no more matches exist
            MatchedPair match = orderBook.takeBestMatch(exchangeOpen);
            if (match == null) {
                break;  // Exchange closed, no more matches
            }

            // Record the trade in statistics
            stats.recordTrade(match.getExecutionPrice(),
                    match.getExecutedQuantity());

            // Print the match (informational)
            System.out.println("[Engine] MATCHED: " + match);
            matchedTrades++;
        }

        System.out.println("[Engine] Stopped.");
        return "Engine executed " + matchedTrades + " trades.";
    }
}
