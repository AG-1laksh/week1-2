import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ques7 { // Fixed: Class name now matches the file name

    // Trie Node structure
    private static class TrieNode {
        // ConcurrentHashMap allows safe concurrent insertions
        Map<Character, TrieNode> children = new ConcurrentHashMap<>();
        boolean isEndOfWord = false;
        String fullWord = null;
    }

    private final TrieNode root;
    // Hash table to store global query frequencies
    private final Map<String, Integer> frequencyMap;

    public ques7() { // Fixed: Constructor name matches class name
        this.root = new TrieNode();
        this.frequencyMap = new ConcurrentHashMap<>();

        // Pre-load some sample data
        updateFrequency("java tutorial", 1234567);
        updateFrequency("javascript", 987654);
        updateFrequency("java download", 456789);
        updateFrequency("java 21 features", 2);
    }

    // 1. Update query frequency and add to Trie
    public void updateFrequency(String query, int count) {
        // Update frequency in O(1) time
        frequencyMap.merge(query, count, Integer::sum);

        // Insert into Trie in O(L) time where L is word length
        TrieNode current = root;
        for (char ch : query.toCharArray()) {
            current.children.computeIfAbsent(ch, k -> new TrieNode());
            current = current.children.get(ch);
        }
        current.isEndOfWord = true;
        current.fullWord = query;
    }

    // Convenience method for single searches
    public void updateFrequency(String query) {
        updateFrequency(query, 1);
    }

    // 2. Return top 10 suggestions for any prefix
    public List<String> search(String prefix) {
        TrieNode current = root;

        // Step A: Traverse down to the end of the prefix
        for (char ch : prefix.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return Collections.emptyList(); // Prefix not found
            }
        }

        // Step B: Use a Min-Heap to keep track of the Top 10 results
        PriorityQueue<String> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(frequencyMap::get)
        );

        // Step C: DFS to find all valid words starting from this prefix node
        findAllWords(current, minHeap);

        // Step D: Extract from heap and reverse to get descending order
        List<String> results = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            results.add(minHeap.poll());
        }
        Collections.reverse(results);

        return results;
    }

    // Recursive Depth First Search (DFS) to collect words
    private void findAllWords(TrieNode node, PriorityQueue<String> minHeap) {
        if (node.isEndOfWord) {
            minHeap.offer(node.fullWord);
            if (minHeap.size() > 10) {
                minHeap.poll(); // Kick out the lowest frequency word
            }
        }

        for (TrieNode child : node.children.values()) {
            findAllWords(child, minHeap);
        }
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) {
        ques7 autocomplete = new ques7(); // Fixed: Instantiation uses the new class name

        System.out.println("search(\"java\") \u2192");
        List<String> results = autocomplete.search("java");
        for (int i = 0; i < results.size(); i++) {
            String query = results.get(i);
            System.out.printf("%d. \"%s\" (%d searches)\n", i + 1, query,
                    autocomplete.frequencyMap.get(query));
        }

        System.out.println("\nupdateFrequency(\"java 21 features\")");
        autocomplete.updateFrequency("java 21 features"); // Simulate a new search
        System.out.println("Frequency of \"java 21 features\": " +
                autocomplete.frequencyMap.get("java 21 features"));
    }
}