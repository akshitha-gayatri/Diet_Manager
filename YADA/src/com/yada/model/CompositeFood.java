package com.yada.model;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a composite food made up of other foods (basic or composite).
 * The nutritional values are calculated dynamically based on its components.
 */
public class CompositeFood extends Food {
    private Map<Food, Double> components; // Food -> number of servings

    /**
     * Constructs a new CompositeFood instance.
     *
     * @param id The unique identifier/name of the composite food
     * @param keywords List of keywords associated with the food for searching
     * @param servingSize Description of a single serving (e.g., "1 sandwich")
     */

    public CompositeFood(String id, List<String> keywords, String servingSize) {
        super(id, keywords, servingSize, 0, 0, 0);
        this.components = new HashMap<>();
    }


    /**
     * Adds a component to this composite food.
     *
     * @param food The food component to add
     * @param servings The number of servings of this component
     */

    public void addComponent(Food food, double servings) {
        components.put(food, components.getOrDefault(food, 0.0) + servings);

        // Recalculate nutritional information when components change
        recalculateNutrition();
    }

    /**
     * Removes a component from this composite food.
     *
     * @param food The food component to remove
     */

    public void removeComponent(Food food) {
        components.remove(food);
        recalculateNutrition();
    }


    /**
     * Recalculates the nutritional values based on current components.
     */
    private void recalculateNutrition() {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        for (Map.Entry<Food, Double> entry : components.entrySet()) {
            Food food = entry.getKey();
            double servings = entry.getValue();

            totalCalories += food.getCaloriesPerServing() * servings;
            totalProtein += food.getProtein() * servings;
            totalCarbs += food.getCarbs() * servings;
            totalFats += food.getFats() * servings;
        }

    }

    /**
     * Gets all components of this composite food.
     *
     * @return An unmodifiable map of components and their servings
     */
    public Map<Food, Double> getComponents() {
        return new HashMap<>(components);
    }


    /**
     * Calculates the calories per serving based on current components.
     *
     * @return The total calories per serving of this composite food
     */

    @Override
    public double getCaloriesPerServing() {
        return components.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getCaloriesPerServing() * entry.getValue())
                .sum();
    }
}