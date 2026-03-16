import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ques10 {

    // Thread-safe LRU Cache using LinkedHashMap
    private static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, V> cacheMap;

        public LRUCache(int capacity) {
            this.capacity = capacity;

            this.cacheMap = Collections.synchronizedMap(
                    new LinkedHashMap<K, V>(16, 0.75f, true) {
                        @Override
                        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                            return size() > capacity;
                        }
                    }
            );
        }

        public V get(K key) { return cacheMap.get(key); }
        public void put(K key, V value) { cacheMap.put(key, value); }
        public void remove(K key) { cacheMap.remove(key); }
        public boolean containsKey(K key) { return cacheMap.containsKey(key); }
    }

    // Cache Tiers
    private final LRUCache<String, String> l1Cache;
    private final LRUCache<String, String> l2Cache;

    // Access tracker
    private final Map<String, Integer> accessCounts;

    // Promotion Thresholds
    private static final int L2_PROMOTION_THRESHOLD = 1;
    private static final int L1_PROMOTION_THRESHOLD = 5;

    // Latencies
    private static final double L1_LATENCY = 0.5;
    private static final double L2_LATENCY = 5.0;
    private static final double L3_LATENCY = 150.0;

    // Metrics
    private final AtomicInteger l1Hits = new AtomicInteger(0);
    private final AtomicInteger l2Hits = new AtomicInteger(0);
    private final AtomicInteger l3Hits = new AtomicInteger(0);

    private int totalRequests = 0;
    private double totalLookupTime = 0.0;

    public ques10() {
        this.l1Cache = new LRUCache<>(10000);
        this.l2Cache = new LRUCache<>(100000);
        this.accessCounts = new ConcurrentHashMap<>();
    }

    // Fetch Video
    public synchronized String getVideo(String videoId) {

        System.out.println("getVideo(\"" + videoId + "\")");

        totalRequests++;
        double requestTime = 0.0;

        int accesses = accessCounts.merge(videoId, 1, Integer::sum);

        // L1 Cache
        if (l1Cache.containsKey(videoId)) {
            l1Hits.incrementAndGet();
            requestTime += L1_LATENCY;
            totalLookupTime += requestTime;

            System.out.println("→ L1 Cache HIT (" + L1_LATENCY + "ms)");
            return "Video Data for " + videoId;
        }

        System.out.println("→ L1 Cache MISS (" + L1_LATENCY + "ms)");
        requestTime += L1_LATENCY;

        // L2 Cache
        if (l2Cache.containsKey(videoId)) {
            l2Hits.incrementAndGet();
            requestTime += L2_LATENCY;
            totalLookupTime += requestTime;

            System.out.println("→ L2 Cache HIT (" + L2_LATENCY + "ms)");

            if (accesses >= L1_PROMOTION_THRESHOLD) {
                promoteToL1(videoId, "Video Data for " + videoId);
                System.out.println("→ Promoted to L1");
            }

            System.out.println("→ Total: " + requestTime + "ms");
            return "Video Data for " + videoId;
        }

        System.out.println("→ L2 Cache MISS");

        // L3 Database
        l3Hits.incrementAndGet();
        requestTime += L3_LATENCY;
        totalLookupTime += requestTime;

        System.out.println("→ L3 Database HIT (" + L3_LATENCY + "ms)");

        String videoData = fetchFromDatabase(videoId);

        if (accesses >= L2_PROMOTION_THRESHOLD) {
            l2Cache.put(videoId, videoData);
            System.out.println("→ Added to L2 (access count: " + accesses + ")");
        }

        System.out.println("→ Total: " + requestTime + "ms");

        return videoData;
    }

    // Simulate DB
    private String fetchFromDatabase(String videoId) {
        return "Video Data for " + videoId;
    }

    // Promotion
    private void promoteToL1(String videoId, String data) {
        l2Cache.remove(videoId);
        l1Cache.put(videoId, data);
    }

    // Statistics
    public synchronized String getStatistics() {

        if (totalRequests == 0) return "No data yet.";

        double l1Rate = (l1Hits.get() * 100.0) / totalRequests;
        double l2Rate = (l2Hits.get() * 100.0) / totalRequests;
        double l3Rate = (l3Hits.get() * 100.0) / totalRequests;

        double overallRate = l1Rate + l2Rate;
        double avgTime = totalLookupTime / totalRequests;

        return String.format(
                "L1: Hit Rate %.0f%%, Avg Time: %.1fms\n" +
                        "L2: Hit Rate %.0f%%, Avg Time: %.1fms\n" +
                        "L3: Hit Rate %.0f%%, Avg Time: %.0fms\n" +
                        "Overall: Hit Rate %.0f%%, Avg Time: %.1fms",
                l1Rate, L1_LATENCY,
                l2Rate, L2_LATENCY,
                l3Rate, L3_LATENCY,
                overallRate, avgTime
        );
    }

    // Main method
    public static void main(String[] args) {

        ques10 cache = new ques10();

        cache.getVideo("video1");
        cache.getVideo("video2");
        cache.getVideo("video1");
        cache.getVideo("video1");
        cache.getVideo("video1");
        cache.getVideo("video1");

        System.out.println("\nStatistics:");
        System.out.println(cache.getStatistics());
    }
}
