import java.util.*;
import java.util.concurrent.*;

public class ques5 { // Fixed: Class name now matches the file name

    // Simple data class for incoming events
    public static class PageViewEvent {
        String url;
        String userId;
        String source;

        public PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // HashMaps for multidimensional tracking
    private final Map<String, Integer> pageViews;
    private final Map<String, Set<String>> uniqueVisitors;
    private final Map<String, Integer> trafficSources;

    // Total events processed (useful for percentage calculations)
    private int totalEvents = 0;

    public ques5() { // Fixed: Constructor name matches class name
        // ConcurrentHashMaps ensure high-throughput thread safety
        this.pageViews = new ConcurrentHashMap<>();
        this.uniqueVisitors = new ConcurrentHashMap<>();
        this.trafficSources = new ConcurrentHashMap<>();

        // Background thread to simulate the "batch update every 5 seconds"
        ScheduledExecutorService dashboardUpdater = Executors.newSingleThreadScheduledExecutor();
        dashboardUpdater.scheduleAtFixedRate(this::printDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // 1. Process incoming page view events in real-time
    public void processEvent(PageViewEvent event) {
        synchronized (this) {
            totalEvents++;
        }

        // Increment visit count for the page in O(1) time
        pageViews.merge(event.url, 1, Integer::sum);

        // Track unique visitor using a thread-safe Set
        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet()).add(event.userId);

        // Count traffic sources
        trafficSources.merge(event.source, 1, Integer::sum);
    }

    // 2. Generate and print the dashboard
    public void printDashboard() {
        if (totalEvents == 0) return;

        System.out.println("\n--- Real-Time Analytics Dashboard ---");

        // 3. Maintain top 10 most visited pages using a PriorityQueue (Min-Heap)
        // This is O(N log K) which is much faster than sorting the entire map O(N log N)
        PriorityQueue<Map.Entry<String, Integer>> topPages = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue)
        );

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            topPages.offer(entry);
            if (topPages.size() > 10) {
                topPages.poll(); // Remove the smallest element to keep only top 10
            }
        }

        // Extract from heap and reverse to get descending order
        List<Map.Entry<String, Integer>> sortedTopPages = new ArrayList<>();
        while (!topPages.isEmpty()) {
            sortedTopPages.add(topPages.poll());
        }
        Collections.reverse(sortedTopPages);

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedTopPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", rank++, url, views, uniques);
        }

        // 4. Count visits by traffic source and calculate percentages
        System.out.println("\nTraffic Sources:");
        List<String> sourceStats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            String source = entry.getKey();
            int count = entry.getValue();

            // Capitalize first letter of source
            String formattedSource = source.substring(0, 1).toUpperCase() + source.substring(1);
            int percentage = (int) Math.round(((double) count / totalEvents) * 100);
            sourceStats.add(formattedSource + ": " + percentage + "%");
        }
        System.out.println(String.join(", ", sourceStats));
        System.out.println("-------------------------------------\n");
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) throws InterruptedException {
        ques5 dashboard = new ques5(); // Fixed: Instantiation uses the new class name

        // Simulate high throughput event stream
        dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_123", "google"));
        dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_456", "facebook"));
        dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_123", "direct")); // user_123 returns
        dashboard.processEvent(new PageViewEvent("/sports/championship", "user_789", "google"));
        dashboard.processEvent(new PageViewEvent("/sports/championship", "user_999", "google"));
        dashboard.processEvent(new PageViewEvent("/sports/championship", "user_888", "other"));

        // Wait a bit so the background thread has time to print the 5-second dashboard update
        Thread.sleep(6000);

        System.exit(0);
    }
}
