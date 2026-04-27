package StockTradingExchangeSimulator;

public class MatchedPair {
    private final Order buyOrder;
    private final Order sellOrder;
    private final double executionPrice;   // (buy.price + sell.price) / 2
    private final int executedQuantity;    // min(buy.quantity, sell.quantity)

    /**
     * Create a matched pair from a buy and sell order.
     * Validates that orders are for the same stock and price aligns.
     */
    public MatchedPair(Order buyOrder, Order sellOrder) {
        if (!buyOrder.getStock().equals(sellOrder.getStock())) {
            throw new IllegalArgumentException(
                    "Orders must be for same stock");
        }
        if (buyOrder.getPrice() < sellOrder.getPrice()) {
            throw new IllegalArgumentException(
                    "Buy price must be >= sell price");
        }

        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.executionPrice = (buyOrder.getPrice() +
                sellOrder.getPrice()) / 2.0;
        this.executedQuantity = Math.min(buyOrder.getQuantity(),
                sellOrder.getQuantity());
    }

    // Getters
    public Order getBuyOrder() { return buyOrder; }
    public Order getSellOrder() { return sellOrder; }
    public double getExecutionPrice() { return executionPrice; }
    public int getExecutedQuantity() { return executedQuantity; }

    @Override
    public String toString() {
        return String.format(
                "BUY %s @%.2f x%d <-> SELL %s @%.2f x%d => " +
                        "Executed @%.2f x%d",
                buyOrder.getStock(), buyOrder.getPrice(),
                buyOrder.getQuantity(),
                sellOrder.getStock(), sellOrder.getPrice(),
                sellOrder.getQuantity(),
                executionPrice, executedQuantity);
    }
}
