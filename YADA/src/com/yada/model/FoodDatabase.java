package com.yada.model;
import java.io.*;
import java.util.*;


/**
 * Manages the collection of all foods in the system, including both basic and composite foods.
 * Provides functionality for adding, removing, searching, and persisting food data.
 * Supports loading from and saving to file storage, with a defined file format.
 */

public class FoodDatabase {
    private List<Food> foods;
    private static final String DEFAULT_FILENAME = "foods.txt";

    /**
     * Constructs a new empty FoodDatabase.
     */
    public FoodDatabase() {
        foods = new ArrayList<>();
    }

    /**
     * Adds a food item to the database.
     *
     * @param food The food to add (cannot be null)
     * @throws IllegalArgumentException if food is null
     */
    public void addFood(Food food) {
        if (food == null) throw new IllegalArgumentException("Food cannot be null");
        foods.add(food);
    }

    /**
     * Removes a food item from the database.
     *
     * @param food The food to remove (cannot be null)
     * @return true if the food was found and removed, false otherwise
     * @throws IllegalArgumentException if food is null
     */
    public boolean removeFood(Food food) {
        if (food == null) throw new IllegalArgumentException("Food cannot be null");
        return foods.remove(food);
    }


    /**
     * Gets a defensive copy of all foods in the database.
     *
     * @return A new ArrayList containing all foods (never null, may be empty)
     */
    public List<Food> getAllFoods() {
        return new ArrayList<>(foods);
    }


    /**
     * Gets all basic foods in the database.
     *
     * @return A list of BasicFood instances (never null, may be empty)
     */
    public List<BasicFood> getBasicFoods() {
        return foods.stream()
                .filter(f -> f instanceof BasicFood)
                .map(f -> (BasicFood) f)
                .collect(java.util.stream.Collectors.toList());
    }


    /**
     * Gets all composite foods in the database.
     *
     * @return A list of CompositeFood instances (never null, may be empty)
     */
    public List<CompositeFood> getCompositeFoods() {
        return foods.stream()
                .filter(f -> f instanceof CompositeFood)
                .map(f -> (CompositeFood) f)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds foods that match the given keywords.
     *
     * @param keywords The list of keywords to search for (null or empty returns all foods)
     * @param matchAll If true, requires all keywords to match; if false, any keyword match
     * @return A list of matching foods (never null, may be empty)
     */
    public List<Food> findFoodsByKeywords(List<String> keywords, boolean matchAll) {
        if (keywords.isEmpty()) {
            return getAllFoods();
        }

        return foods.stream()
                .filter(food -> matchAll ?
                        food.matchesAllKeywords(keywords) :
                        food.matchesAnyKeyword(keywords))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds a food by its unique identifier.
     *
     * @param id The food ID to search for (case-sensitive)
     * @return The matching Food object, or null if not found
     */
    public Food getFoodById(String id) {
        return foods.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    /**
     * Saves all foods to the default file.
     *
     * @throws IOException if there's an error writing to the file
     */
    public void saveToFile() throws IOException {
        saveToFile(DEFAULT_FILENAME);
    }


    /**
     * Saves all foods to a specified file.
     *
     * @param filename The file to save to (cannot be null)
     * @throws IOException if there's an error writing to the file
     * @throws IllegalArgumentException if filename is null
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Food food : foods) {
                if (food instanceof BasicFood) {
                    writer.println("BASIC:" + food.getId() + ":" +
                            String.join(",", food.getKeywords()) + ":" +
                            food.getServingSize() + ":" +
                            food.getCaloriesPerServing() + ":" +
                            food.getProtein() + ":" +
                            food.getCarbs() + ":" +
                            food.getFats());
                } else if (food instanceof CompositeFood) {
                    CompositeFood composite = (CompositeFood) food;
                    writer.print("COMPOSITE:" + food.getId() + ":" +
                            String.join(",", food.getKeywords()) + ":" +
                            food.getServingSize());

                    for (Map.Entry<Food, Double> component : composite.getComponents().entrySet()) {
                        writer.print(":" + component.getKey().getId() +
                                "=" + component.getValue());
                    }
                    writer.println();
                }
            }
        }
    }

    /**
     * Loads foods from the default file.
     *
     * @throws IOException if there's an error reading the file
     */
    public void loadFromFile() throws IOException {
        loadFromFile(DEFAULT_FILENAME);
    }

    /**
     * Loads foods from a specified file.
     * Uses a two-pass approach to properly handle composite food components.
     *
     * @param filename The file to load from (cannot be null)
     * @throws IOException if there's an error reading the file
     * @throws IllegalArgumentException if filename is null
     */
    public void loadFromFile(String filename) throws IOException {
        foods.clear();
        Map<String, CompositeFood> composites = new HashMap<>();

        // First pass: Load all basic foods
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts[0].equals("BASIC")) {
                    String id = parts[1];
                    List<String> keywords = Arrays.asList(parts[2].split(","));
                    String servingSize = parts[3];
                    double calories = Double.parseDouble(parts[4]);
                    double protein = Double.parseDouble(parts[5]);
                    double carbs = Double.parseDouble(parts[6]);
                    double fats = Double.parseDouble(parts[7]);

                    foods.add(new BasicFood(id, keywords, servingSize, calories, protein, carbs, fats));
                } else if (parts[0].equals("COMPOSITE")) {
                    String id = parts[1];
                    List<String> keywords = Arrays.asList(parts[2].split(","));
                    String servingSize = parts[3];

                    CompositeFood composite = new CompositeFood(id, keywords, servingSize);
                    foods.add(composite);
                    composites.put(id, composite);
                }
            }
        }

        // Second pass: Link composite foods to their components
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts[0].equals("COMPOSITE")) {
                    String id = parts[1];
                    CompositeFood composite = composites.get(id);

                    // Start from index 4 to skip id, keywords, and serving size
                    for (int i = 4; i < parts.length; i++) {
                        String[] componentParts = parts[i].split("=");
                        String componentId = componentParts[0];
                        double servings = Double.parseDouble(componentParts[1]);

                        Food component = getFoodById(componentId);
                        if (component != null) {
                            composite.addComponent(component, servings);
                        }
                    }
                }
            }
        }
    }

    /**
     * Interface for food data sources to implement for importing foods.
     */
    public interface FoodDataSource {
        List<BasicFood> fetchFoods();
    }

    /**
     * Imports foods from an external data source.
     *
     * @param source The data source to import from (cannot be null)
     * @throws IllegalArgumentException if source is null
     */
    public void importFromSource(FoodDataSource source) {
        if (source == null) throw new IllegalArgumentException("Source cannot be null");
        List<BasicFood> importedFoods = source.fetchFoods();
        foods.addAll(importedFoods);
    }

}