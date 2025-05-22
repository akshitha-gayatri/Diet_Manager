package com.yada.model;
import java.util.List;

/**
 * Represents a basic food item with fixed nutritional values per serving.
 * This class extends the abstract Food class and provides concrete implementation
 * for calorie calculation based on fixed values.
 */
public class BasicFood extends Food {
    /**
     * Constructs a new BasicFood instance.
     * id The unique identifier/name of the food
     * keywords List of keywords associated with the food for searching
     * servingSize Description of a single serving (e.g., "1 medium apple")
     * caloriesPerServing Calories contained in one serving
     * protein Protein content in grams per serving
     * carbs Carbohydrate content in grams per serving
     * fats Fat content in grams per serving
     */
    private double caloriesPerServing;

    public BasicFood(String id, List<String> keywords, String servingSize,
                     double caloriesPerServing,
                     double protein, double carbs, double fats) {
        super(id, keywords, servingSize, protein, carbs, fats);
        this.caloriesPerServing = caloriesPerServing;
    }

    /**
     * Gets the calories per serving for this basic food.
     *
     * returns The number of calories in one serving
     */
    @Override
    public double getCaloriesPerServing() {
        return caloriesPerServing;
    }
}