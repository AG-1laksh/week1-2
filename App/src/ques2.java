import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ques2 { // Fixed: Class name now matches the file name

    // HashMap for instant O(1) stock lookup
    private final Map<String, Integer> stockMap;

    // LinkedHashMap used as a FIFO waiting list: Maps productId -> (userId -> timestamp)
    private final Map<String, Map<String, Long>> waitingLists;

    public ques2() { // Fixed: Constructor name matches class name
        // ConcurrentHashMap allows fast, non-blocking reads for stock checks
        this.stockMap = new ConcurrentHashMap<>();
        this.waitingLists = new ConcurrentHashMap<>();

        // Initialize the flash sale item
        stockMap.put("IPHONE15_256GB", 100);

        // We use a LinkedHashMap to maintain insertion order (FIFO) for the waiting list.
        // It is wrapped in a synchronized map to keep it thread-safe.
        waitingLists.put("IPHONE15_256GB", Collections.synchronizedMap(new LinkedHashMap<>()));
    }

    // 1. Instant stock availability check in O(1) time
    public String checkStock(String productId) {
        int stock = stockMap.getOrDefault(productId, 0);
        return stock + " units available";
    }

    // 2. Process purchase safely
    // The synchronized keyword ensures only one thread can execute this block at a time,
    // preventing the race condition where two users read "1 stock" and both buy it.
    public synchronized String purchaseItem(String productId, String userId) {
        int currentStock = stockMap.getOrDefault(productId, 0);

        if (currentStock > 0) {
            // Decrement stock
            stockMap.put(productId, currentStock - 1);
            return "Success, " + (currentStock - 1) + " units remaining";
        } else {
            // Handle Out of Stock & Waiting List
            Map<String, Long> waitList = waitingLists.get(productId);
            if (waitList != null) {
                // putIfAbsent prevents the same user from taking up multiple spots
                waitList.putIfAbsent(userId, System.currentTimeMillis());
                return "Added to waiting list, position #" + waitList.size();
            }
            return "Product unavailable.";
        }
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) {
        ques2 manager = new ques2();

        System.out.println("checkStock(\"IPHONE15_256GB\") -> " +
                manager.checkStock("IPHONE15_256GB"));

        // Simulate two quick successful purchases
        System.out.println("purchaseItem(\"IPHONE15_256GB\", \"userId=12345\") -> "
                + manager.purchaseItem("IPHONE15_256GB", "userId=12345"));
        System.out.println("purchaseItem(\"IPHONE15_256GB\", \"userId=67890\") -> "
                + manager.purchaseItem("IPHONE15_256GB", "userId=67890"));

        // Simulate 98 more purchases to exhaust the stock completely
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", "userId=bulk_" + i);
        }

        // Try to buy when stock is empty to trigger the waiting list
        System.out.println("purchaseItem(\"IPHONE15_256GB\", \"userId=99999\") -> "
                + manager.purchaseItem("IPHONE15_256GB", "userId=99999"));

        System.out.println("purchaseItem(\"IPHONE15_256GB\", \"userId=88888\") -> "
                + manager.purchaseItem("IPHONE15_256GB", "userId=88888"));
    }
}