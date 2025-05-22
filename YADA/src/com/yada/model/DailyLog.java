package com.yada.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages daily food consumption logs with undo functionality.
 * Tracks food entries by date and provides methods for adding, removing,
 * and querying food consumption data.
 */
public class DailyLog {
    private final TreeMap<LocalDate, List<Object[]>> dailyLogs; // [foodId, servings, date]
    private static final String LOG_FILE = "daily_logs.txt";
    private static final Map<String, String> foodIdCache = new HashMap<>();
    private final Deque<Runnable> undoStack = new ArrayDeque<>(); // Changed to Deque for clarity
    private boolean isLoaded = false;

    /**
     * Represents a single food entry in the daily log.
     */

    public static class FoodEntry {
        private final String foodId;
        private final double servings;
        private final LocalDate date;

        /**
         * Constructs a new FoodEntry.
         *
         * @param foodId The ID of the food consumed
         * @param servings Number of servings consumed
         * @param date The date of consumption
         */

        public FoodEntry(String foodId, double servings, LocalDate date) {
            this.foodId = foodId;
            this.servings = servings;
            this.date = date;
        }

        public String getFoodId() { return foodId; }
        public double getServings() { return servings; }
        public LocalDate getDate() { return date; }
    }


    /**
     * Interns a food ID string to optimize memory usage.
     * @param foodId The food ID to intern
     * @return The canonical representation of the food ID
     */

    private static String internFoodId(String foodId) {
        return foodIdCache.computeIfAbsent(foodId, k -> k);
    }

    /** Constructs a new, empty DailyLog instance. */
    public DailyLog() {
        dailyLogs = new TreeMap<>();
    }

    /**
     * Adds a food consumption entry to the log.
     *
     * @param food The food that was consumed (cannot be null)
     * @param servings The number of servings consumed (must be positive)
     * @param date The date of consumption (cannot be null)
     * @throws IllegalArgumentException if parameters are invalid
     */

    public void addFoodEntry(Food food, double servings, LocalDate date) {
        ensureLoaded();
        String internedId = internFoodId(food.getId());
        Object[] entry = {internedId, servings, date};

        Map<LocalDate, List<Object[]>> snapshotBefore = deepCopyCurrentState();

        dailyLogs.computeIfAbsent(date, k -> new ArrayList<>()).add(entry);

        try {
            saveToFile();
        } catch (IOException e) {
            System.err.println("Error saving daily logs: " + e.getMessage());
            return;
        }

        undoStack.push(() -> {
            dailyLogs.clear();
            dailyLogs.putAll(snapshotBefore);
            try {
                saveToFile();
            } catch (IOException ex) {
                System.err.println("Error during undo save: " + ex.getMessage());
            }
        });
    }

    /**
     * Removes a food entry from the log.
     *
     * @param date The date of the entry to remove
     * @param entryToRemove The entry to remove (cannot be null)
     * @return true if the entry was found and removed, false otherwise
     * @throws IllegalArgumentException if entryToRemove is null
     */

    public boolean removeFoodEntry(LocalDate date, FoodEntry entryToRemove) {
        ensureLoaded();
        List<Object[]> entries = dailyLogs.get(date);
        if (entries == null) return false;

        Map<LocalDate, List<Object[]>> snapshotBefore = deepCopyCurrentState();

        boolean removed = entries.removeIf(entry ->
                entry[0].equals(entryToRemove.getFoodId()) &&
                        (double)entry[1] == entryToRemove.getServings() &&
                        entry[2].equals(entryToRemove.getDate())
        );

        if (!removed) return false;

        // Clean up empty dates
        if (entries.isEmpty()) {
            dailyLogs.remove(date);
        }

        // Save to file
        try {
            saveToFile();
        } catch (IOException e) {
            System.err.println("Error saving daily logs: " + e.getMessage());
            return false;
        }

        // Create undo operation that restores previous state
        undoStack.push(() -> {
            dailyLogs.clear();
            dailyLogs.putAll(snapshotBefore);
            try {
                saveToFile();
            } catch (IOException ex) {
                System.err.println("Error during undo save: " + ex.getMessage());
            }
        });

        return true;
    }


    /**
     * Creates a deep copy of the current log state for undo functionality.
     * This method preserves the complete state of the log at the time of calling,
     * including all dates and their associated food entries.
     *
     * @return A new TreeMap containing a deep copy of all current log entries,
     *         with each entry's data array also copied to prevent reference sharing
     */

    private Map<LocalDate, List<Object[]>> deepCopyCurrentState() {
        Map<LocalDate, List<Object[]>> copy = new TreeMap<>();
        dailyLogs.forEach((date, entries) -> {
            List<Object[]> entriesCopy = new ArrayList<>(entries.size());
            for (Object[] entry : entries) {
                entriesCopy.add(Arrays.copyOf(entry, entry.length));
            }
            copy.put(date, entriesCopy);
        });
        return copy;
    }


    /**
     * Retrieves all food entries for a specific date as FoodEntry objects.
     * The returned list is a transformed view of the internal data structure.
     *
     * @param date The date to query (cannot be null)
     * @return An unmodifiable list of FoodEntry objects for the specified date.
     *         Returns empty list if no entries exist for the date.
     * @throws IllegalStateException if the log data hasn't been loaded yet
     */
    public List<FoodEntry> getFoodEntriesForDate(LocalDate date) {
        ensureLoaded();
        List<Object[]> rawEntries = dailyLogs.getOrDefault(date, Collections.emptyList());
        return rawEntries.stream()
                .map(arr -> new FoodEntry((String) arr[0], (double) arr[1], (LocalDate) arr[2]))
                .collect(Collectors.toList());
    }

    /**
     * Saves all log entries to persistent storage in pipe-delimited format:
     * date|foodId|servings
     *
     * @throws IOException if there's an error writing to the log file,
     *         including permission issues or filesystem problems
     */
    public void saveToFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE))) {
            for (List<Object[]> entries : dailyLogs.values()) {
                for (Object[] entry : entries) {
                    writer.println(entry[2] + "|" + entry[0] + "|" + entry[1]);
                }
            }
        }
    }

    /**
     * Loads log entries from persistent storage, initializing the internal data structure.
     * Silently skips malformed entries while logging errors to stderr.
     *
     * @param database The food database (currently unused, can be null)
     * @throws IOException if there's an error reading the log file,
     *         including file not found (though this case is handled gracefully)
     */
    public void loadFromFile(FoodDatabase database) throws IOException {
        if (!Files.exists(Paths.get(LOG_FILE))) {
            return;
        }

        dailyLogs.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    try {
                        LocalDate date = LocalDate.parse(parts[0]);
                        String foodId = internFoodId(parts[1]);
                        double servings = Double.parseDouble(parts[2]);

                        dailyLogs.computeIfAbsent(date, k -> new ArrayList<>())
                                .add(new Object[]{foodId, servings, date});
                    } catch (Exception e) {
                        System.err.println("Error parsing log entry: " + line);
                    }
                }
            }
        }
        isLoaded = true;
    }

    /**
     * Retrieves all log entries across all dates as FoodEntry objects.
     * The returned map is a transformed view of the internal data structure.
     *
     * @return An unmodifiable TreeMap where keys are dates in chronological order
     *         and values are lists of FoodEntry objects for each date
     */
    public Map<LocalDate, List<FoodEntry>> getAllEntries() {
        ensureLoaded();
        Map<LocalDate, List<FoodEntry>> result = new TreeMap<>();
        dailyLogs.forEach((date, rawEntries) -> {
            result.put(date, rawEntries.stream()
                    .map(arr -> new FoodEntry((String) arr[0], (double) arr[1], (LocalDate) arr[2]))
                    .collect(Collectors.toList()));
        });
        return result;
    }

    /**
     * Reverts the most recent add or remove operation by executing the top undo action.
     * Has no effect if there are no operations to undo.
     */
    public void undoLastAction() {
        ensureLoaded();
        if (!undoStack.isEmpty()) {
            Runnable undoAction = undoStack.pop();
            undoAction.run();
        }
    }

    /**
     * Checks if there are operations available to undo.
     *
     * @return true if there are undoable operations in the stack, false otherwise
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Ensures log data is loaded before operations, performing lazy loading if needed.
     * This method handles the initialization of log data from persistent storage
     * on first access.
     */

    private void ensureLoaded() {
        if (!isLoaded) {
            try {
                loadFromFile(null);
                isLoaded = true;
            } catch (IOException e) {
                System.err.println("Error loading logs: " + e.getMessage());
            }
        }
    }
}