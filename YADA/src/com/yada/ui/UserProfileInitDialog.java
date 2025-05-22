/**
 * A dialog for initializing user profile information when the application starts for the first time.
 * This dialog collects essential user details including name, gender, height, age, weight,
 * and activity level to create a personalized profile for calorie tracking.
 */
package com.yada.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;

import com.yada.model.UserProfile;
import com.yada.model.UserProfile.CalorieCalculationMethod;
import com.yada.model.UserProfile.ActivityLevel;

import java.io.IOException;
import java.time.LocalDate;

public class UserProfileInitDialog extends Dialog<UserProfile> {
    /**
     * UI Color Constants:
     * - PRIMARY_COLOR: Main primary color (#2c3e50)
     * - SECONDARY_COLOR: Secondary color (#34495e)
     * - ACCENT_COLOR: Accent color (#3498db)
     * - SUCCESS_COLOR: Success state color (#27ae60)
     * - WARNING_COLOR: Warning state color (#f39c12)
     * - DANGER_COLOR: Danger/error state color (#e74c3c)
     * - LIGHT_TEXT: Light text color (#ecf0f1)
     * - DARK_TEXT: Dark text color (#2c3e50)
     * - BG_COLOR: Background color (#f5f7fa)
     * - CARD_BG: Card/panel background color (#ffffff)
     */
    // Move color constants to class level
    private static final String PRIMARY_COLOR = "#2c3e50";
    private static final String SECONDARY_COLOR = "#34495e";
    private static final String ACCENT_COLOR = "#3498db";
    private static final String SUCCESS_COLOR = "#27ae60";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String LIGHT_TEXT = "#ecf0f1";
    private static final String DARK_TEXT = "#2c3e50";
    private static final String BG_COLOR = "#f5f7fa";
    private static final String CARD_BG = "#ffffff";

    /**
     * Constructs a new UserProfileInitDialog with all necessary input fields.
     * The dialog is modal and must be completed before continuing with the application.
     */
    public UserProfileInitDialog() {
        /**
         * Generates CSS style for buttons based on the provided color.
         *
         * @param color the background color for the button
         * @return a CSS style string for the button
         */
        setTitle("User Profile Setup");
        setHeaderText("Please enter your profile details");
        initModality(Modality.APPLICATION_MODAL);

        // Style the dialog
        DialogPane dialogPane = getDialogPane();
        try {
            // Correct path to CSS file (assuming it's in src/main/resources/com/yada/ui/)
            dialogPane.getStylesheets().add(
                    getClass().getResource("/com/yada/ui/styles.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: " + e.getMessage());
        }
        dialogPane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Name input
        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        // Gender selection
        Label genderLabel = new Label("Gender:");
        genderLabel.setStyle("-fx-font-weight: bold;");
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        RadioButton femaleRadio = new RadioButton("Female");
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(15, maleRadio, femaleRadio);
        grid.add(genderLabel, 0, 1);
        grid.add(genderBox, 1, 1);

        // Height input
        Label heightLabel = new Label("Height (cm):");
        heightLabel.setStyle("-fx-font-weight: bold;");
        TextField heightField = new TextField();
        heightField.setPromptText("Height in cm");
        heightField.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        grid.add(heightLabel, 0, 2);
        grid.add(heightField, 1, 2);

        // Age input
        Label ageLabel = new Label("Age:");
        ageLabel.setStyle("-fx-font-weight: bold;");
        TextField ageField = new TextField();
        ageField.setPromptText("Age in years");
        ageField.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        grid.add(ageLabel, 0, 3);
        grid.add(ageField, 1, 3);

        // Weight input
        Label weightLabel = new Label("Weight (kg):");
        weightLabel.setStyle("-fx-font-weight: bold;");
        TextField weightField = new TextField();
        weightField.setPromptText("Weight in kg");
        weightField.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        grid.add(weightLabel, 0, 4);
        grid.add(weightField, 1, 4);

        // Activity level selection
        Label activityLabel = new Label("Activity Level:");
        activityLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<ActivityLevel> activityLevelCombo = new ComboBox<>();
        activityLevelCombo.getItems().addAll(ActivityLevel.values());
        activityLevelCombo.setPromptText("Select Activity Level");
        activityLevelCombo.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        grid.add(activityLabel, 0, 5);
        grid.add(activityLevelCombo, 1, 5);

        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Style the buttons
        dialogPane.getButtonTypes().stream()
                .map(dialogPane::lookupButton)
                .forEach(button -> {
                    if (button instanceof Button) {
                        Button btn = (Button) button;
                        if (btn.getText().equals("Submit")) {
                            btn.setStyle(getButtonStyle(SUCCESS_COLOR));
                        } else {
                            btn.setStyle(getButtonStyle(ACCENT_COLOR));
                        }
                    }
                });

        setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    // Validate inputs
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        showError("Name cannot be empty");
                        return null;
                    }

                    String gender = maleRadio.isSelected() ? "Male" :
                            femaleRadio.isSelected() ? "Female" : null;
                    if (gender == null) {
                        showError("Please select a gender");
                        return null;
                    }

                    double height = Double.parseDouble(heightField.getText().trim());
                    if (height <= 0) {
                        showError("Height must be a positive number");
                        return null;
                    }

                    int age = Integer.parseInt(ageField.getText().trim());
                    if (age <= 0 || age > 120) {
                        showError("Please enter a valid age");
                        return null;
                    }

                    double weight = Double.parseDouble(weightField.getText().trim());
                    if (weight <= 0) {
                        showError("Weight must be a positive number");
                        return null;
                    }

                    ActivityLevel activityLevel = activityLevelCombo.getValue();
                    if (activityLevel == null) {
                        showError("Please select an activity level");
                        return null;
                    }

                    // Create and return UserProfile with initialized data
                    UserProfile profile = new UserProfile(name, gender, height);
                    profile.setCalorieMethod(CalorieCalculationMethod.HARRIS_BENEDICT);
                    boolean updated = profile.updateProfileForDate(LocalDate.now(), age, weight, activityLevel);
                    if (!updated) {
                        showError("Failed to initialize profile data");
                        return null;
                    }

                    try {
                        profile.saveToFile();
                    } catch (IOException e) {
                        showError("Failed to save profile: " + e.getMessage());
                    }
                    return profile;

                } catch (NumberFormatException e) {
                    showError("Please enter valid numeric values for height, age, and weight");
                    return null;
                }
            }
            return null;
        });

        Platform.runLater(() -> nameField.requestFocus());
    }

    private String getButtonStyle(String color) {
        /**
         * Displays an error alert dialog with the specified message.
         *
         * @param message the error message to display
         */
        return "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10 5 10; " +
                "-fx-background-radius: 3; " +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);" +
                "-fx-font-size: 12px;" +
                "-fx-min-width: 120px;";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}