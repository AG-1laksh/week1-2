import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ques4 { // Fixed: Class name now matches the new file name

    // Custom Entry class to store the IP and expiration time
    private static class DNSEntry {
        String ipAddress;
        long expirationTime;

        DNSEntry(String ipAddress, long ttlSeconds) {
            this.ipAddress = ipAddress;
            // Calculate absolute expiration time in milliseconds
            this.expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    private final Map<String, DNSEntry> cache;
    private final int MAX_CAPACITY = 100;

    // Performance metrics
    private int hits = 0;
    private int misses = 0;
    private double totalLookupTimeMs = 0;

    public ques4() { // Fixed: Constructor name matches class name
        // LinkedHashMap configured for Access-Order (true) to support LRU eviction
        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                // Automatically removes the least recently used entry if we exceed capacity
                return size() > MAX_CAPACITY;
            }
        });

        // Background thread to clean expired entries every 10 seconds
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::removeExpiredEntries, 10, 10, TimeUnit.SECONDS);
    }

    // 1. Resolve domain to IP
    public synchronized String resolve(String domain) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                // Cache HIT
                hits++;
                double hitLookupTime = (System.nanoTime() - startTime) / 1_000_000.0;
                totalLookupTimeMs += hitLookupTime;
                return "Cache HIT \u2192 " + entry.ipAddress + " (retrieved in " +
                        String.format("%.2f", hitLookupTime) + "ms)";
            } else {
                // Cache EXPIRED - clean it up before querying upstream
                cache.remove(domain);
            }
        }

        // Cache MISS or EXPIRED
        misses++;
        String status = (entry == null) ? "Cache MISS" : "Cache EXPIRED";

        // Simulate querying upstream DNS (which would normally take ~100ms)
        String newIp = queryUpstreamDNS(domain);
        long ttl = 300; // 300 seconds TTL for simulation

        cache.put(domain, new DNSEntry(newIp, ttl));

        double missLookupTime = (System.nanoTime() - startTime) / 1_000_000.0;
        totalLookupTimeMs += missLookupTime; // For stats calculation

        return status + " \u2192 Query upstream \u2192 " + newIp + " (TTL: " + ttl + "s)";
    }

    // 2. Mock upstream DNS query
    private String queryUpstreamDNS(String domain) {
        try {
            // Simulate network delay
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Return a mock IP address
        return "172.217.14." + (int)(Math.random() * 255);
    }

    // 3. Background cleanup task
    private synchronized void removeExpiredEntries() {
        // removeIf safely iterates and removes expired entries
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // 4. Report Cache Hit/Miss Ratios
    public synchronized String getCacheStats() {
        int totalRequests = hits + misses;
        if (totalRequests == 0) return "No requests yet.";

        double hitRate = ((double) hits / totalRequests) * 100;
        double avgLookupTime = totalLookupTimeMs / totalRequests;

        return String.format("Hit Rate: %.1f%%, Avg Lookup Time: %.2fms", hitRate, avgLookupTime);
    }

    // Main method to test
    public static void main(String[] args) throws InterruptedException {
        ques4 dnsCache = new ques4(); // Fixed: Instantiation uses the new class name

        System.out.println("resolve(\"google.com\") \u2192 " + dnsCache.resolve("google.com"));
        System.out.println("resolve(\"google.com\") \u2192 " + dnsCache.resolve("google.com"));

        System.out.println("... waiting for a moment to show stats ...");

        System.out.println("getCacheStats() \u2192 " + dnsCache.getCacheStats());

        // We won't wait a full 301 seconds here, but you can see how the logic handles it!
        System.exit(0); // Exit to stop the background thread
    }
}