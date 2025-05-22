package com.yada.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's nutritional profile with personal information,
 * activity tracking, and calorie management. Maintains daily entries
 * for weight, activity level, and calorie consumption.
 */
public class UserProfile {
    private String name;
    private String gender;
    private double height; // in cm
    private Map<LocalDate, ProfileEntry> profileEntries;
    private CalorieCalculationMethod calorieMethod;

    /**
     * Enumeration of supported calorie calculation methods.
     */
    public enum CalorieCalculationMethod {
        /** Harris-Benedict equation (revised) */
        HARRIS_BENEDICT,
        /** Mifflin-St Jeor equation (more modern) */
        MIFFLIN_ST_JEOR
    }

    /**
     * Enumeration of activity levels with corresponding multipliers
     * for calculating total daily energy expenditure (TDEE).
     */
    public enum ActivityLevel {
        /** Little or no exercise (multiplier: 1.2) */
        SEDENTARY(1.2),
        /** Light exercise 1-3 days/week (multiplier: 1.375) */
        LIGHTLY_ACTIVE(1.375),
        /** Moderate exercise 3-5 days/week (multiplier: 1.55) */
        MODERATELY_ACTIVE(1.55),
        /** Hard exercise 6-7 days/week (multiplier: 1.725) */
        VERY_ACTIVE(1.725),
        /** Very hard exercise & physical job (multiplier: 1.9) */
        EXTRA_ACTIVE(1.9);

        private final double multiplier;

        /**
         * Creates an ActivityLevel with the specified multiplier.
         * @param multiplier The activity multiplier for TDEE calculation
         */
        ActivityLevel(double multiplier) {
            this.multiplier = multiplier;
        }

        /**
         * Gets the activity multiplier for this level.
         * @return The multiplier value
         */
        public double getMultiplier() {
            return multiplier;
        }
    }

    /**
     * Represents a single day's profile entry with nutritional targets
     * and consumption tracking.
     */
    public static class ProfileEntry {
        private int age;
        private double weight; // in kg
        private ActivityLevel activityLevel;
        private double targetCalories;
        private double consumedCalories;
        private boolean updateMade;

        /**
         * Creates a new ProfileEntry with initial values.
         * @param age The user's age in years
         * @param weight The user's weight in kilograms
         * @param activityLevel The user's activity level
         * @param targetCalories The calculated target calories for the day
         */
        public ProfileEntry(int age, double weight, ActivityLevel activityLevel, double targetCalories) {
            this.age = age;
            this.weight = weight;
            this.activityLevel = activityLevel;
            this.targetCalories = targetCalories;
            this.consumedCalories = 0;
            this.updateMade = false;
        }

        // Getters and setters with documentation
        /** @return The user's age in years */
        public int getAge() { return age; }

        /** @param age The new age value in years */
        public void setAge(int age) { this.age = age; }

        /** @return The user's weight in kilograms */
        public double getWeight() { return weight; }

        /** @param weight The new weight value in kilograms */
        public void setWeight(double weight) { this.weight = weight; }

        /** @return The current activity level */
        public ActivityLevel getActivityLevel() { return activityLevel; }

        /** @param activityLevel The new activity level */
        public void setActivityLevel(ActivityLevel activityLevel) { this.activityLevel = activityLevel; }

        /** @return The target calories for the day */
        public double getTargetCalories() { return targetCalories; }

        /** @param targetCalories The new target calorie value */
        public void setTargetCalories(double targetCalories) { this.targetCalories = targetCalories; }

        /** @return The total calories consumed so far today */
        public double getConsumedCalories() { return consumedCalories; }

        /** @param consumedCalories The new consumed calories value */
        public void setConsumedCalories(double consumedCalories) { this.consumedCalories = consumedCalories; }

        /** @return Whether this entry has been manually updated */
        public boolean isUpdateMade() { return updateMade; }

        /** @param updateMade Flag indicating if this entry was manually updated */
        public void setUpdateMade(boolean updateMade) { this.updateMade = updateMade; }
    }

    /**
     * Creates a new UserProfile with basic information.
     * @param name The user's name
     * @param gender The user's gender ("Male" or "Female" for calorie calculations)
     * @param height The user's height in centimeters
     */
    public UserProfile(String name, String gender, double height) {
        this.name = name;
        this.gender = gender;
        this.height = height;
        this.profileEntries = new HashMap<>();
        this.calorieMethod = CalorieCalculationMethod.HARRIS_BENEDICT;
    }

    /**
     * Saves the profile data to a file in key-value format.
     * @throws IOException if there's an error writing to the file
     */
    public void saveToFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("user_profile.txt"))) {
            writer.write("Name:" + name + "\n");
            writer.write("Gender:" + gender + "\n");
            writer.write("Height:" + height + "\n");
            writer.write("CalorieMethod:" + calorieMethod + "\n");

            // Save all profile entries
            for (Map.Entry<LocalDate, ProfileEntry> entry : profileEntries.entrySet()) {
                writer.write("EntryDate:" + entry.getKey() + "\n");
                writer.write("Age:" + entry.getValue().getAge() + "\n");
                writer.write("Weight:" + entry.getValue().getWeight() + "\n");
                writer.write("ActivityLevel:" + entry.getValue().getActivityLevel() + "\n");
                writer.write("TargetCalories:" + entry.getValue().getTargetCalories() + "\n");
                writer.write("ConsumedCalories:" + entry.getValue().getConsumedCalories() + "\n");
                writer.write("UpdateMade:" + entry.getValue().isUpdateMade() + "\n");
            }
        }
    }

    /**
     * Loads profile data from file.
     * @return The loaded UserProfile, or null if file doesn't exist
     * @throws IOException if there's an error reading the file
     */
    public static UserProfile loadFromFile() throws IOException {
        if (!Files.exists(Paths.get("user_profile.txt"))) {
            return null;
        }

        UserProfile profile = null;
        LocalDate currentDate = null;
        ProfileEntry currentEntry = null;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("user_profile.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    switch (parts[0]) {
                        case "Name":
                            profile = new UserProfile(parts[1], "", 0);
                            break;
                        case "Gender":
                            if (profile != null) profile.gender = parts[1];
                            break;
                        case "Height":
                            if (profile != null) profile.height = Double.parseDouble(parts[1]);
                            break;
                        case "CalorieMethod":
                            if (profile != null) profile.calorieMethod = CalorieCalculationMethod.valueOf(parts[1]);
                            break;
                        case "EntryDate":
                            currentDate = LocalDate.parse(parts[1]);
                            break;
                        case "Age":
                            if (currentDate != null && profile != null) {
                                currentEntry = new ProfileEntry(Integer.parseInt(parts[1]), 0, ActivityLevel.SEDENTARY, 0);
                                profile.profileEntries.put(currentDate, currentEntry);
                            }
                            break;
                        case "Weight":
                            if (currentEntry != null) currentEntry.setWeight(Double.parseDouble(parts[1]));
                            break;
                        case "ActivityLevel":
                            if (currentEntry != null) currentEntry.setActivityLevel(ActivityLevel.valueOf(parts[1]));
                            break;
                        case "TargetCalories":
                            if (currentEntry != null) currentEntry.setTargetCalories(Double.parseDouble(parts[1]));
                            break;
                        case "ConsumedCalories":
                            if (currentEntry != null) currentEntry.setConsumedCalories(Double.parseDouble(parts[1]));
                            break;
                        case "UpdateMade":
                            if (currentEntry != null) currentEntry.setUpdateMade(Boolean.parseBoolean(parts[1]));
                            break;
                    }
                }
            }
        }
        return profile;
    }

    /**
     * Gets or creates a profile entry for the specified date.
     * If no entry exists, creates one using previous day's values or defaults.
     * @param date The date to get entry for
     * @return The ProfileEntry for the requested date (never null)
     */
    public ProfileEntry getProfileEntryForDate(LocalDate date) {
        if (!profileEntries.containsKey(date)) {
            ProfileEntry previousEntry = getPreviousDayEntry(date);

            int entryAge;
            double entryWeight;
            ActivityLevel entryActivityLevel;

            if (previousEntry != null) {
                entryAge = previousEntry.getAge();
                entryWeight = previousEntry.getWeight();
                entryActivityLevel = previousEntry.getActivityLevel();
            } else {
                // Default values if no previous entry exists
                entryAge = 30;
                entryWeight = 70.0;
                entryActivityLevel = ActivityLevel.MODERATELY_ACTIVE;
            }

            // Calculate target calories using current method
            double targetCalories = calculateTargetCalories(entryAge, entryWeight, entryActivityLevel);
            ProfileEntry newEntry = new ProfileEntry(entryAge, entryWeight, entryActivityLevel, targetCalories);
            profileEntries.put(date, newEntry);
        }
        return profileEntries.get(date);
    }

    /**
     * Gets the profile entry for the previous day.
     * @param date The date to check previous day for
     * @return The previous day's entry, or null if none exists
     */
    private ProfileEntry getPreviousDayEntry(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        return profileEntries.get(previousDay);
    }

    /**
     * Updates profile information for a specific date.
     * @param date The date to update
     * @param age The new age value
     * @param weight The new weight in kg
     * @param activityLevel The new activity level
     * @return true if update was successful, false otherwise
     */
    public boolean updateProfileForDate(LocalDate date, int age, double weight, ActivityLevel activityLevel) {
        ProfileEntry entry = getProfileEntryForDate(date);
        if (entry != null) {
            entry.setAge(age);
            entry.setWeight(weight);
            entry.setActivityLevel(activityLevel);
            // Recalculate target calories with current method
            double targetCalories = calculateTargetCalories(age, weight, activityLevel);
            entry.setTargetCalories(targetCalories);
            entry.setUpdateMade(true);
            return true;
        }
        return false;
    }

    /**
     * Records additional calories consumed on a specific date.
     * @param date The date of consumption
     * @param calories The number of calories to add
     */
    public void recordConsumedCalories(LocalDate date, double calories) {
        ProfileEntry entry = getProfileEntryForDate(date);
        if (entry != null) {
            entry.setConsumedCalories(entry.getConsumedCalories() + calories);
        }
    }

    /**
     * Gets calorie information for a specific date.
     * @param date The date to query
     * @return Map containing target, consumed, and difference values,
     *         or empty map if no entry exists
     */
    public Map<String, Double> getCalorieInformationForDate(LocalDate date) {
        ProfileEntry entry = getProfileEntryForDate(date);
        Map<String, Double> calorieInfo = new HashMap<>();
        if (entry != null) {
            calorieInfo.put("Target Calories", entry.getTargetCalories());
            calorieInfo.put("Consumed Calories", entry.getConsumedCalories());
            calorieInfo.put("Calorie Difference", entry.getTargetCalories() - entry.getConsumedCalories());
        }
        return calorieInfo;
    }

    /**
     * Calculates target calories based on current method.
     * @param age The user's age
     * @param weight The user's weight in kg
     * @param activityLevel The user's activity level
     * @return The calculated target calories
     * @throws IllegalStateException if calorie method is invalid
     */
    private double calculateTargetCalories(int age, double weight, ActivityLevel activityLevel) {
        switch (calorieMethod) {
            case HARRIS_BENEDICT:
                return calculateHarrisBenedictCalories(age, weight, activityLevel);
            case MIFFLIN_ST_JEOR:
                return calculateMifflinStJeorCalories(age, weight, activityLevel);
            default:
                throw new IllegalStateException("Unexpected calorie calculation method");
        }
    }

    /**
     * Calculates calories using Harris-Benedict equation.
     * @param age The user's age
     * @param weight The user's weight in kg
     * @param activityLevel The user's activity level
     * @return The calculated TDEE
     */
    private double calculateHarrisBenedictCalories(int age, double weight, ActivityLevel activityLevel) {
        double bmr;
        if (gender.equalsIgnoreCase("Male")) {
            bmr = 66.5 + (13.75 * weight) + (5.003 * height) - (6.755 * age);
        } else {
            bmr = 655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age);
        }
        return bmr * activityLevel.getMultiplier();
    }

    /**
     * Calculates calories using Mifflin-St Jeor equation.
     * @param age The user's age
     * @param weight The user's weight in kg
     * @param activityLevel The user's activity level
     * @return The calculated TDEE
     */
    private double calculateMifflinStJeorCalories(int age, double weight, ActivityLevel activityLevel) {
        double bmr;
        if (gender.equalsIgnoreCase("Male")) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
        return bmr * activityLevel.getMultiplier();
    }

    // Standard getters and setters with documentation

    /** @return The user's name */
    public String getName() { return name; }

    /** @return The user's gender */
    public String getGender() { return gender; }

    /** @return The user's height in cm */
    public double getHeight() { return height; }

    /** @return The current calorie calculation method */
    public CalorieCalculationMethod getCalorieMethod() { return calorieMethod; }

    /**
     * Sets the calorie calculation method.
     * @param method The new calculation method
     */
    public void setCalorieMethod(CalorieCalculationMethod method) { this.calorieMethod = method; }

    /**
     * Gets all profile entries.
     * @return A defensive copy of all entries
     */
    public Map<LocalDate, ProfileEntry> getAllProfileEntries() { return new HashMap<>(profileEntries); }

    /**
     * Records calories consumed today (convenience method).
     * @param calories The number of calories to record
     */
    public void recordConsumedCalories(double calories) {
        recordConsumedCalories(LocalDate.now(), calories);
    }

    /**
     * Gets calorie information for today (convenience method).
     * @return Map with today's calorie information
     */
    public Map<String, Double> getTodayCalorieInformation() {
        return getCalorieInformationForDate(LocalDate.now());
    }
}