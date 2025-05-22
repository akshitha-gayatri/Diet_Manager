package com.yada.model;

import java.io.*;
import java.util.*;

/**
 * Abstract base class for all food types in the system.
 * Provides common properties and methods for all food items,
 * including nutritional information and keyword searching capabilities.
 */

public abstract class Food implements Serializable {
    private String id;
    private List<String> keywords;
    private String servingSize; // New field to describe serving size
    private double protein; // New nutritional fields
    private double carbs;
    private double fats;


    /**
     * Constructs a new Food instance.
     *
     * @param id The unique identifier/name of the food
     * @param keywords List of keywords associated with the food
     * @param servingSize Description of a single serving
     * @param protein Protein content in grams per serving
     * @param carbs Carbohydrate content in grams per serving
     * @param fats Fat content in grams per serving
     */

    public Food(String id, List<String> keywords, String servingSize,
                double protein, double carbs, double fats) {
        this.id = id;
        this.keywords = new ArrayList<>(keywords);
        this.servingSize = servingSize;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }

    /**
     * Gets the unique identifier/name of this food item.
     *
     * @return The food's ID as a String (never null)
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a copy of all keywords associated with this food.
     * The returned list is a defensive copy to prevent external modification.
     *
     * @return A new ArrayList containing all keywords (never null, may be empty)
     */
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }


    /**
     * Adds a new keyword to this food's keyword list if not already present.
     * Keyword comparison is case-sensitive.
     *
     * @param keyword The keyword to add (null values will be silently ignored)
     */
    public void addKeyword(String keyword) {
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }

    /**
     * Checks if this food matches a given keyword (case-insensitive).
     * A match occurs when any of the food's keywords contains the search keyword.
     *
     * @param keyword The keyword to search for (null-safe: returns false for null input)
     * @return true if any keyword contains the search term (case-insensitive), false otherwise
     */

    public boolean matchesKeyword(String keyword) {
        return keywords.stream()
                .anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * Checks if this food matches ALL given search keywords (case-insensitive).
     *
     * @param searchKeywords List of keywords to match against (null-safe: returns true for empty/null list)
     * @return true if ALL keywords are matched, false otherwise
     */

    public boolean matchesAllKeywords(List<String> searchKeywords) {
        return searchKeywords.stream()
                .allMatch(this::matchesKeyword);
    }

    /**
     * Checks if this food matches ANY of the given search keywords (case-insensitive).
     *
     * @param searchKeywords List of keywords to match against (null-safe: returns false for empty/null list)
     * @return true if ANY keyword is matched, false otherwise
     */
    public boolean matchesAnyKeyword(List<String> searchKeywords) {
        return searchKeywords.stream()
                .anyMatch(this::matchesKeyword);
    }

    /**
     * Abstract method to get the calories per serving.
     * Must be implemented by concrete food classes.
     *
     * @return The number of calories in one serving (always >= 0)
     */
    public abstract double getCaloriesPerServing();

    /**
     * Gets the description of a standard serving size.
     *
     * @return The serving size description (e.g., "1 cup", "100g") (never null)
     */
    public String getServingSize() {
        return servingSize;
    }

    /**
     * Gets the protein content per serving.
     *
     * @return Protein amount in grams (always >= 0)
     */
    public double getProtein() {
        return protein;
    }


    /**
     * Gets the carbohydrate content per serving.
     *
     * @return Carbohydrate amount in grams (always >= 0)
     */
    public double getCarbs() {
        return carbs;
    }

    /**
     * Gets the fat content per serving.
     *
     * @return Fat amount in grams (always >= 0)
     */
    public double getFats() {
        return fats;
    }

    /**
     * Calculates the sum of all macronutrients (protein + carbs + fats).
     *
     * @return Total macronutrient content in grams (always >= 0)
     */
    public double getTotalNutrients() {
        return protein + carbs + fats;
    }


    /**
     * Returns a string representation of this food (its ID).
     *
     * @return The food's ID string (same as getId())
     */
    @Override
    public String toString() {
        return id;
    }

}

