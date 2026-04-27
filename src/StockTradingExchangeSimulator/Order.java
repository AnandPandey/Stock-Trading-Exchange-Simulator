package StockTradingExchangeSimulator;

public class Order {
    public enum OrderType { BUY, SELL }

    private final String orderId;      // e.g., "ORD-0042"
    private final String traderName;   // e.g., "Trader-1"
    private final OrderType type;      // BUY or SELL
    private final String stock;        // e.g., "RELIANCE", "TCS", "INFY"
    private final double price;        // limit price in rupees
    private final int quantity;        // number of shares
    private final long timestamp;      // System.nanoTime() at creation

    /**
     * Create a new order. Timestamp is automatically set to current nanoTime.
     */
    public Order(String orderId, String traderName, OrderType type,
                 String stock, double price, int quantity) {
        this.orderId = orderId;
        this.traderName = traderName;
        this.type = type;
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }

    // Getters (no setters - immutable!)
    public String getOrderId() { return orderId; }
    public String getTraderName() { return traderName; }
    public OrderType getType() { return type; }
    public String getStock() { return stock; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        String typeStr = (type == OrderType.BUY) ? "BUY " : "SELL";
        return String.format("%s %-10s x%-3d @ ₹ %.2f [%s by %s]",
                typeStr, stock, quantity, price, orderId, traderName);
    }
}