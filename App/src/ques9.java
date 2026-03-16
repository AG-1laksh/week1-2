import java.util.*;

public class ques9 { // Fixed: Class name now matches the file name

    public static class Transaction {
        int id;
        int amount;
        String merchant;
        String time; // Format: "HH:mm"
        String accountId;

        public Transaction(int id, int amount, String merchant, String time, String accountId) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.time = time;
            this.accountId = accountId;
        }

        @Override
        public String toString() {
            return "id:" + id;
        }
    }

    private List<Transaction> transactions;

    public ques9() { // Fixed: Constructor name matches class name
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // 1. Classic Two-Sum: O(N) time complexity
    public List<String> findTwoSum(int target) {
        // Map<ComplementAmount, Transaction>
        Map<Integer, Transaction> complements = new HashMap<>();
        List<String> results = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            // If the current transaction's amount is already in our map as a needed complement, we found a pair!
            if (complements.containsKey(t.amount)) {
                Transaction match = complements.get(t.amount);
                results.add("[" + match.toString() + ", " + t.toString() + "]");
            } else {
                // Otherwise, store what THIS transaction needs to hit the target
                complements.put(complement, t);
            }
        }
        return results;
    }

    // 2. Duplicate Detection: Same amount, same merchant, different accounts
    public List<String> detectDuplicates() {
        // Map<"amount_merchant", List<Transaction>>
        Map<String, List<Transaction>> groupingMap = new HashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            groupingMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : groupingMap.entrySet()) {
            List<Transaction> group = entry.getValue();
            if (group.size() > 1) {
                Set<String> accounts = new HashSet<>();
                for (Transaction t : group) {
                    accounts.add(t.accountId);
                }
                // If there are multiple transactions but multiple distinct accounts
                if (accounts.size() > 1) {
                    Transaction first = group.get(0);
                    duplicates.add("{amount:" + first.amount + ", merchant:\"" + first.merchant +
                            "\", accounts:" + accounts + "}");
                }
            }
        }
        return duplicates;
    }

    // 3. K-Sum: Recursive approach to find K transactions summing to target
    public List<String> findKSum(int k, int target) {
        List<Transaction> currentPath = new ArrayList<>();
        List<List<Transaction>> results = new ArrayList<>();

        // Helper method kicks off the recursion
        kSumHelper(transactions, k, target, 0, currentPath, results);

        List<String> formattedResults = new ArrayList<>();
        for (List<Transaction> result : results) {
            formattedResults.add(result.toString());
        }
        return formattedResults;
    }

    private void kSumHelper(List<Transaction> txs, int k, int target, int start,
                            List<Transaction> currentPath, List<List<Transaction>> results) {
        // Base Case
        if (k == 0) {
            if (target == 0) {
                results.add(new ArrayList<>(currentPath));
            }
            return;
        }

        for (int i = start; i < txs.size(); i++) {
            Transaction t = txs.get(i);
            currentPath.add(t);
            // Recurse with k-1 and the new reduced target
            kSumHelper(txs, k - 1, target - t.amount, i + 1, currentPath, results);
            // Backtrack
            currentPath.remove(currentPath.size() - 1);
        }
    }

    // Main method to test the Sample Input/Output
    public static void main(String[] args) {
        ques9 system = new ques9(); // Fixed: Instantiation uses the new class name

        // Sample Data
        system.addTransaction(new Transaction(1, 500, "Store A", "10:00", "acc1"));
        system.addTransaction(new Transaction(2, 300, "Store B", "10:15", "acc2"));
        system.addTransaction(new Transaction(3, 200, "Store C", "10:30", "acc3"));
        // Adding a duplicate of Transaction 1 but from a different account
        system.addTransaction(new Transaction(4, 500, "Store A", "10:45", "acc2"));

        System.out.println("findTwoSum(target=500) \u2192 " + system.findTwoSum(500));
        System.out.println("detectDuplicates() \u2192 " + system.detectDuplicates());
        System.out.println("findKSum(k=3, target=1000) \u2192 " + system.findKSum(3, 1000));
    }
}
