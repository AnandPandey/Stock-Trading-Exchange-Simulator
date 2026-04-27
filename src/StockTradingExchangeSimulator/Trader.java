package StockTradingExchangeSimulator;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Trader that generates random buy/sell orders.
 * Implements Callable<String> to return a summary via Future.
 * Checks AtomicBoolean flag to know when to stop.
 * Uses shared AtomicInteger for generating unique order IDs.
 */
public class Trader implements Callable<String> {
    private final String name;
    private final OrderBook orderBook;
    private final AtomicInteger orderIdCounter;  // shared across all traders
    private final AtomicBoolean exchangeOpen;
    private int ordersPlaced = 0;

    // Stock symbols and approximate base prices (INR)
    private static final String[] STOCKS =
            {"RELIANCE", "TCS", "INFY", "HDFC", "WIPRO"};
    private static final double[] BASE_PRICES =
            {2890.00, 3520.00, 1425.00, 1650.00, 450.00};

    /**
     * Create a new trader.
     * @param name Trader name (e.g., "Trader-1")
     * @param orderBook The shared order book
     * @param orderIdCounter Shared atomic counter for generating order IDs
     * @param exchangeOpen Shared flag indicating exchange is running
     */
    public Trader(String name, OrderBook orderBook,
                  AtomicInteger orderIdCounter,
                  AtomicBoolean exchangeOpen) {
        this.name = name;
        this.orderBook = orderBook;
        this.orderIdCounter = orderIdCounter;
        this.exchangeOpen = exchangeOpen;
    }

    @Override
    public String call() throws Exception {
        Random random = new Random();

        while (exchangeOpen.get()) {
            try {
                // Pick random stock
                int stockIdx = random.nextInt(STOCKS.length);
                String stock = STOCKS[stockIdx];
                double basePrice = BASE_PRICES[stockIdx];

                // Price varies ±5% from base
                double variation = (random.nextDouble() - 0.5) * 0.1;
                double price = basePrice * (1 + variation);

                // Random quantity 1-50 shares
                int quantity = 1 + random.nextInt(50);

                // Random buy or sell
                Order.OrderType type = random.nextBoolean() ?
                        Order.OrderType.BUY : Order.OrderType.SELL;

                // Generate unique order ID
                int id = orderIdCounter.incrementAndGet();
                Order order = new Order(String.format("ORD-%04d", id),
                        name, type, stock, price, quantity);

                // Submit order (may block if book is full)
                orderBook.addOrder(order);
                ordersPlaced++;

                System.out.println("[" + name + "] Submitted: " + order);

                // Wait 50-200ms before next order
                Thread.sleep(50 + random.nextInt(150));

            } catch (InterruptedException e) {
                // Mark thread as interrupted and exit
                Thread.currentThread().interrupt();
                break;
            }
        }

        return name + " placed " + ordersPlaced + " orders.";
    }
}
