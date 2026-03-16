import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ques1 { // Fixed: Class name now matches constructor

    // Maps username -> userId for O(1) instant lookup
    private final Map<String, String> registeredUsers;

    // Tracks how many times a username was attempted
    private final Map<String, Integer> attemptFrequency;

    public ques1() {
        // ConcurrentHashMap handles high-throughput concurrent requests safely
        this.registeredUsers = new ConcurrentHashMap<>();
        this.attemptFrequency = new ConcurrentHashMap<>();

        // Pre-load some data for testing
        registeredUsers.put("john_doe", "user_001");
        registeredUsers.put("admin", "user_000");
    }

    // 1. Check availability in O(1) time & Track popularity
    public boolean checkAvailability(String username) {
        // Increment the attempt counter thread-safely
        attemptFrequency.merge(username, 1, Integer::sum);

        // Returns true if the username is NOT in the map
        return !registeredUsers.containsKey(username);
    }

    // 2. Register user if available
    public boolean registerUser(String username, String userId) {
        if (checkAvailability(username)) {
            // putIfAbsent ensures atomic operation (prevents race conditions)
            return registeredUsers.putIfAbsent(username, userId) == null;
        }
        return false;
    }

    // 3. Suggest similar available usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        // Fixed: Moved the dot variant to the top so it actually gets evaluated
        String dotVariant = username.replace("_", ".");
        if (!username.equals(dotVariant) && !registeredUsers.containsKey(dotVariant)) {
            suggestions.add(dotVariant);
        }

        int suffix = 1;
        // Generate up to 3 available suggestions
        while (suggestions.size() < 3) {
            String suggestion1 = username + suffix;
            String suggestion2 = username + "_" + suffix;

            if (!registeredUsers.containsKey(suggestion1) && suggestions.size() < 3) {
                suggestions.add(suggestion1);
            }
            if (!registeredUsers.containsKey(suggestion2) && suggestions.size() < 3) {
                suggestions.add(suggestion2);
            }
            suffix++;
        }

        return suggestions;
    }

    // 4. Get the most attempted username
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxAttempts = 0;

        // Iterate through the frequency map to find the highest value
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted + " (" + maxAttempts + " attempts)";
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) {
        ques1 system = new ques1();

        // Simulate some failed attempts on "admin" to build up frequency
        for(int i = 0; i < 10543; i++) {
            system.checkAvailability("admin");
        }

        System.out.println("checkAvailability(\"john_doe\") -> " +
                system.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " +
                system.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") -> " +
                system.suggestAlternatives("john_doe"));
        System.out.println("getMostAttempted() -> " + system.getMostAttempted());
    }
}