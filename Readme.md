# Stock Trading Exchange Simulator - Compilation & Execution

## Prerequisites
- Java 8 or higher
- `javac` and `java` command available

## Files Overview

1. **Order.java** - Immutable order class (Task 1)
2. **MatchedPair.java** - Matched order pair (Task 1)
3. **OrderBook.java** - Thread-safe order book with ReentrantLock (Task 2)
4. **ExchangeStats.java** - Thread-safe statistics with atomics (Task 3)
5. **Trader.java** - Order generator (Task 3)
6. **MatchingEngine.java** - Order matching consumer (Task 4)
7. **Dashboard.java** - Live statistics display (Task 4)
8. **StockExchange.java** - Main entry point (Task 5)

## Compilation

Compile all classes at once:

```bash
javac Order.java MatchedPair.java OrderBook.java \
       ExchangeStats.java Trader.java MatchingEngine.java \
       Dashboard.java StockExchange.java
```

Or simply:

```bash
javac *.java
```

Expected output: No errors, 8 `.class` files created.

## Execution

Run the simulation:

```bash
java StockExchange
```

The program will:
1. Start 5 traders (using a thread pool of 3)
2. Start the matching engine
3. Start the dashboard (updates every 2 seconds)
4. Run for 15 seconds
5. Gracefully shut down all components
6. Print a final summary

## Expected Output

```
===== EXCHANGE STARTING =====
Traders: 5 | Thread Pool: 3 | Duration: 15 seconds
=============================

[Trader-1] Submitted: BUY  RELIANCE x24  @ ₹ 2996.29 [ORD-0001 by Trader-1]
[Trader-2] Submitted: SELL INFY  x30  @ ₹ 1425.50 [ORD-0002 by Trader-2]
[Engine]   MATCHED: BUY TCS @3520.00 x15 <-> SELL TCS @3510.00 x15 => Executed @3515.00 x15

===== EXCHANGE DASHBOARD =====
Trades: 12 | Volume: 340 | Last: 1427.75 | High: 3550.00 | Low: 892.25
Pending orders: 37 (18 buy, 19 sell)
=============================

... (continues for 15 seconds) ...

===== INITIATING SHUTDOWN =====

===== EXCHANGE CLOSING =====
Trader-1 placed 42 orders.
Trader-2 placed 38 orders.
Trader-3 placed 45 orders.
Trader-4 placed 39 orders.
Trader-5 placed 40 orders.
Engine executed 87 trades.

===== FINAL SUMMARY =====
Trades: 87 | Volume: 2190 | Last: 1847.55 | High: 3550.00 | Low: 450.12
Total orders placed: 205
Unmatched orders: 31 (17 buy, 14 sell)
=========================

All pools shut down successfully.
```

## Troubleshooting

### Compilation Errors

**"cannot find symbol" errors:**
- Ensure all `.java` files are in the same directory
- Verify file names match exactly (case-sensitive)

**"class X is public, should be declared in a file named X.java":**
- Remove `public` modifiers from class declarations in non-main files
- Or ensure each file name matches its public class

### Runtime Errors

**InterruptedException:**
- Normal during shutdown - the code properly handles thread interruption

**Program hangs:**
- This should not happen with the refactored code
- If it does, use Ctrl+C to terminate
- Check that `orderBook.wakeAll()` is called before shutdown

**ConcurrentModificationException:**
- Indicates a synchronization bug
- Should not occur with ReentrantLock implementation

## Performance Notes

- **Dashboard updates every 2 seconds** - not every trade
- **Traders sleep 50-200ms** between orders to simulate realistic behavior
- **Engine uses 200ms timeout** on conditions to check shutdown flag
- **Thread pool reuses 3 threads** for 5 traders - demonstrates efficient scheduling

## Testing Variations

### Test with smaller duration:
```java
// In StockExchange.java, change:
Thread.sleep(15000);  // 15 seconds
// To:
Thread.sleep(5000);   // 5 seconds
```

### Test with different order book capacity:
```java
// In StockExchange.java, change:
OrderBook orderBook = new OrderBook(100);  // 100 orders max
// To:
OrderBook orderBook = new OrderBook(20);   // Tighter capacity
```

### Test with more traders:
```java
// In StockExchange.java, change:
for (int i = 1; i <= 5; i++) {
// To:
for (int i = 1; i <= 10; i++) {
```

## Key Design Principles

1. **Immutable Objects** - Order class has no setters
2. **Fair Locking** - ReentrantLock(true) prevents trader starvation
3. **Condition Variables** - Two separate conditions reduce wakeups
4. **Atomic Types** - Used for simple counters that don't need full locking
5. **ReadWriteLock** - Multiple threads read stats simultaneously
6. **Graceful Shutdown** - Proper sequence: flag → wakeAll() → shutdown() → get() → awaitTermination()
7. **Callable + Future** - Threads return results instead of silently completing

## Concurrency Concepts Demonstrated

| Concept | Usage |
|---------|-------|
| Thread Pools | Fixed and single-thread executors |
| Immutability | Order, MatchedPair |
| ReentrantLock | OrderBook synchronization |
| Condition Variables | ordersAvailable, bookNotFull |
| Atomic Variables | orderIdCounter, totalTrades, totalVolume |
| ReadWriteLock | ExchangeStats price tracking |
| Callable + Future | Traders, engine, result collection |
| ScheduledExecutor | Dashboard scheduling |
| Graceful Shutdown | shutdown() → awaitTermination() pattern |
