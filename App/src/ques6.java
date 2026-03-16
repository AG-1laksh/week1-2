import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ques6 { // Fixed: Class name now matches the file name

    // Represents the result of a rate limit check
    public static class RateLimitResult {
        boolean allowed;
        long remaining;
        long retryAfterSeconds;

        public RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remaining = remaining;
            this.retryAfterSeconds = retryAfterSeconds;
        }
    }

    // The Token Bucket implementation
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillIntervalMs; // Milliseconds per 1 token
        private long currentTokens;
        private long lastRefillTimestamp;

        public TokenBucket(long maxTokens, long tokensPerWindow, long windowSizeMs) {
            this.maxTokens = maxTokens;
            this.currentTokens = maxTokens;
            // Calculate how many milliseconds it takes to earn exactly 1 token
            this.refillIntervalMs = windowSizeMs / tokensPerWindow;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        // Synchronized to handle concurrent requests from the SAME client safely
        public synchronized RateLimitResult tryConsume() {
            refill();

            if (currentTokens > 0) {
                currentTokens--;
                return new RateLimitResult(true, currentTokens, 0);
            } else {
                // Calculate time until exactly 1 token is regenerated
                long timeUntilNextTokenMs = refillIntervalMs - (System.currentTimeMillis() - lastRefillTimestamp);
                return new RateLimitResult(false, 0, Math.max(1, timeUntilNextTokenMs / 1000));
            }
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRefillTimestamp;
            long tokensToAdd = elapsedTime / refillIntervalMs;

            if (tokensToAdd > 0) {
                currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
                // Only advance the timestamp by the exact multiple of intervals to prevent "leaking" time
                lastRefillTimestamp += tokensToAdd * refillIntervalMs;
            }
        }

        public synchronized String getStatus() {
            refill();
            long used = maxTokens - currentTokens;
            // Simplification: showing the timestamp when the NEXT token arrives
            long resetTimestamp = (lastRefillTimestamp + refillIntervalMs) / 1000;
            return String.format("{used: %d, limit: %d, reset: %d}", used, maxTokens, resetTimestamp);
        }
    }

    // Hash table to store a bucket for every client
    private final Map<String, TokenBucket> clientBuckets;

    // Constants for the rate limit policy
    private final long MAX_REQUESTS = 1000;
    private final long WINDOW_SIZE_MS = 60 * 60 * 1000; // 1 hour in milliseconds

    public ques6() { // Fixed: Constructor name matches class name
        this.clientBuckets = new ConcurrentHashMap<>();
    }

    // 1. Process Rate Limit Check in O(1) time
    public String checkRateLimit(String clientId) {
        // computeIfAbsent creates a new bucket if this is the client's first request
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(MAX_REQUESTS, MAX_REQUESTS, WINDOW_SIZE_MS)
        );

        RateLimitResult result = bucket.tryConsume();

        if (result.allowed) {
            return "Allowed (" + result.remaining + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after " + result.retryAfterSeconds + "s)";
        }
    }

    // 2. Fetch the current status for a client
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) {
            return "{used: 0, limit: " + MAX_REQUESTS + ", reset: 0}";
        }
        return bucket.getStatus();
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) throws InterruptedException {
        ques6 rateLimiter = new ques6(); // Fixed: Instantiation uses the new class name
        String clientId = "abc123";

        // Simulate 2 initial requests
        System.out.println("checkRateLimit(\"" + clientId + "\") \u2192 " +
                rateLimiter.checkRateLimit(clientId));
        System.out.println("checkRateLimit(\"" + clientId + "\") \u2192 " +
                rateLimiter.checkRateLimit(clientId));

        // Drain the remaining 998 tokens instantly to simulate a burst
        for (int i = 0; i < 998; i++) {
            rateLimiter.checkRateLimit(clientId);
        }

        // This request will be denied because the bucket is empty
        System.out.println("checkRateLimit(\"" + clientId + "\") \u2192 " +
                rateLimiter.checkRateLimit(clientId));

        // Print the status
        System.out.println("getRateLimitStatus(\"" + clientId + "\") \u2192 " +
                rateLimiter.getRateLimitStatus(clientId));
    }
}