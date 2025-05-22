/**
 * The main UI class for the YADA (Your Awesome Diet Assistant) application.
 * This class provides a graphical interface for managing food database entries,
 * creating and editing basic and composite foods, tracking daily nutrition,
 * and managing user profiles.
 */

package com.yada.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.yada.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class FoodDatabaseUI extends Application {
    /**
     * Starts the JavaFX application, initializing the main window and UI components.
     *
     * @param primaryStage the primary stage for this application
     */
    private FoodDatabase database;
    private ObservableList<Food> foodList = FXCollections.observableArrayList();
    private TableView<Food> foodTable;
    private UserProfile userProfile;
    private DailyLog dailyLog;

    // CSS styles
    private final String mainBackground = "-fx-background-color: #f8f9fa;";
    private final String panelBackground = "-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;";
    private final String buttonPrimary = "-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String buttonSuccess = "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String buttonWarning = "-fx-background-color: #fbbc05; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String buttonDanger = "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String titleStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3c4043;";
    private final String subtitleStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5f6368;";
    @Override
    public void start(Stage primaryStage) {
        /**
         * Displays a dialog for editing basic food entries.
         *
         * @param food the BasicFood object to edit
         */
        database = new FoodDatabase();
        foodList = FXCollections.observableArrayList();
        dailyLog = new DailyLog();
        try {
            dailyLog.loadFromFile(database);
        } catch (IOException e) {
            showError("Could not load daily logs: " + e.getMessage());
        }
        loadInitialData();

        if (userProfile == null) {
            UserProfileInitDialog profileDialog = new UserProfileInitDialog();
            Optional<UserProfile> result = profileDialog.showAndWait();

            if (result.isPresent()) {
                userProfile = result.get();
            } else {
                Platform.exit();
                return;
            }
        }

        GridPane root = new GridPane();
        root.setPadding(new Insets(15));
        root.setStyle(mainBackground);
        root.setHgap(15); // Add horizontal gap between columns

        // Create columns
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(70); // 70% width for main content

        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(30); // 30% width for right panel

        root.getColumnConstraints().addAll(leftColumn, rightColumn);
        // Create top panel and center panel
        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(createTopPanel(), createCenterPanel());
        GridPane.setVgrow(mainContent, Priority.ALWAYS);

        // Add components to grid
        root.add(mainContent, 0, 0); // Main content in column 0 (left)
        root.add(createRightPanel(), 1, 0); // Right panel in column 1 (right)

        // Make the right panel span the full height
        GridPane.setVgrow(root.getChildren().get(1), Priority.ALWAYS);

        Scene scene = new Scene(root, 1100, 800);

        // Set font to Times New Roman for the entire application

        // Apply CSS styling
        //scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("YADA - Your Awesome Diet Assistant");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        primaryStage.setOnCloseRequest(e -> {
            try {
                database.saveToFile();
                dailyLog.saveToFile();
            } catch (IOException ex) {
                showError("Could not save food database: " + ex.getMessage());
            }
        });

        primaryStage.show();
    }

    private void showEditBasicFoodDialog(BasicFood food) {
        /**
         * Displays a dialog for editing composite food entries.
         *
         * @param food the CompositeFood object to edit
         */
        Dialog<BasicFood> dialog = new Dialog<>();
        dialog.setTitle("Edit Basic Food");
        dialog.setHeaderText("Edit details for " + food.getId());
        dialog.getDialogPane().getStyleClass().add("edit-dialog");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(food.getId());
        TextField keywordsField = new TextField(String.join(", ", food.getKeywords()));
        TextField servingSizeField = new TextField(food.getServingSize());
        TextField caloriesField = new TextField(String.valueOf(food.getCaloriesPerServing()));
        TextField proteinField = new TextField(String.valueOf(food.getProtein()));
        TextField carbsField = new TextField(String.valueOf(food.getCarbs()));
        TextField fatsField = new TextField(String.valueOf(food.getFats()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Keywords:"), 0, 1);
        grid.add(keywordsField, 1, 1);
        grid.add(new Label("Serving Size:"), 0, 2);
        grid.add(servingSizeField, 1, 2);
        grid.add(new Label("Calories:"), 0, 3);
        grid.add(caloriesField, 1, 3);
        grid.add(new Label("Protein (g):"), 0, 4);
        grid.add(proteinField, 1, 4);
        grid.add(new Label("Carbs (g):"), 0, 5);
        grid.add(carbsField, 1, 5);
        grid.add(new Label("Fats (g):"), 0, 6);
        grid.add(fatsField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> nameField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    List<String> keywords = Arrays.stream(keywordsField.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String servingSize = servingSizeField.getText().trim();
                    double calories = Double.parseDouble(caloriesField.getText().trim());
                    double protein = Double.parseDouble(proteinField.getText().trim());
                    double carbs = Double.parseDouble(carbsField.getText().trim());
                    double fats = Double.parseDouble(fatsField.getText().trim());

                    // Remove the old food and add the updated one
                    database.removeFood(food);
                    return new BasicFood(name, keywords, servingSize, calories, protein, carbs, fats);
                } catch (NumberFormatException e) {
                    showError("Invalid nutritional values. Please enter numbers.");
                    return null;
                }
            }
            return null;
        });

        Optional<BasicFood> result = dialog.showAndWait();

        result.ifPresent(updatedFood -> {
            database.addFood(updatedFood);
            refreshFoodList();
        });
    }

    private void showEditCompositeFoodDialog(CompositeFood food) {
        /**
         * Creates the top panel containing search controls and action buttons.
         *
         * @return VBox containing the top panel components
         */
        Dialog<CompositeFood> dialog = new Dialog<>();
        dialog.setTitle("Edit Composite Food");
        dialog.setHeaderText("Edit details for " + food.getId());
        dialog.getDialogPane().getStyleClass().add("edit-dialog");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 15, 10, 15));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        TextField nameField = new TextField(food.getId());
        TextField keywordsField = new TextField(String.join(", ", food.getKeywords()));
        TextField servingSizeField = new TextField(food.getServingSize());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Keywords:"), 0, 1);
        grid.add(keywordsField, 1, 1);
        grid.add(new Label("Serving Size:"), 0, 2);
        grid.add(servingSizeField, 1, 2);

        content.getChildren().add(grid);

        // Component selection
        Label componentsLabel = new Label("Components:");
        componentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        content.getChildren().add(componentsLabel);

        TableView<Food> foodSelectionTable = new TableView<>();
        ObservableList<Food> availableFoods = FXCollections.observableArrayList(database.getAllFoods());

        TableColumn<Food, String> nameColumn = new TableColumn<>("Food Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Food, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof BasicFood ? "Basic" : "Composite"));

        TableColumn<Food, Number> caloriesColumn = new TableColumn<>("Calories");
        caloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCaloriesPerServing()));

        foodSelectionTable.getColumns().addAll(nameColumn, typeColumn, caloriesColumn);
        foodSelectionTable.setItems(availableFoods);
        foodSelectionTable.setPrefHeight(150);

        HBox servingBox = new HBox(15);
        servingBox.setPadding(new Insets(5));
        servingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Spinner<Double> servingsSpinner = new Spinner<>(0.1, 10.0, 1.0, 0.1);
        servingsSpinner.setEditable(true);
        servingsSpinner.setPrefWidth(100);

        Button addComponentButton = new Button("Add Component");
        addComponentButton.setStyle(buttonSuccess);

        servingBox.getChildren().addAll(new Label("Servings:"), servingsSpinner, addComponentButton);

        // Component list
        Label selectedComponentsLabel = new Label("Selected Components:");
        selectedComponentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<Map.Entry<Food, Double>> componentsTable = new TableView<>();
        ObservableList<Map.Entry<Food, Double>> selectedComponents = FXCollections.observableArrayList(food.getComponents().entrySet());

        TableColumn<Map.Entry<Food, Double>, String> componentNameColumn = new TableColumn<>("Food");
        componentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getId()));

        TableColumn<Map.Entry<Food, Double>, Number> servingsColumn = new TableColumn<>("Servings");
        servingsColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getValue()));

        TableColumn<Map.Entry<Food, Double>, Number> totalCaloriesColumn = new TableColumn<>("Total Calories");
        totalCaloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(
                        cellData.getValue().getKey().getCaloriesPerServing() * cellData.getValue().getValue()));

        componentsTable.getColumns().addAll(componentNameColumn, servingsColumn, totalCaloriesColumn);
        componentsTable.setItems(selectedComponents);
        componentsTable.setPrefHeight(150);

        Button removeComponentButton = new Button("Remove Selected");
        removeComponentButton.setStyle(buttonDanger);

        // Component selection handling
        final Map<Food, Double> components = new HashMap<>(food.getComponents());

        addComponentButton.setOnAction(e -> {
            Food selectedFood = foodSelectionTable.getSelectionModel().getSelectedItem();
            if (selectedFood != null) {
                double servings = servingsSpinner.getValue();
                components.put(selectedFood, components.getOrDefault(selectedFood, 0.0) + servings);

                // Refresh the components table
                selectedComponents.clear();
                components.entrySet().forEach(selectedComponents::add);
            } else {
                showWarning("Please select a food to add.");
            }
        });

        removeComponentButton.setOnAction(e -> {
            Map.Entry<Food, Double> selectedEntry = componentsTable.getSelectionModel().getSelectedItem();
            if (selectedEntry != null) {
                components.remove(selectedEntry.getKey());

                // Refresh the components table
                selectedComponents.clear();
                components.entrySet().forEach(selectedComponents::add);
            } else {
                showWarning("Please select a component to remove.");
            }
        });

        content.getChildren().addAll(
                foodSelectionTable, servingBox,
                selectedComponentsLabel, componentsTable, removeComponentButton
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(600);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    List<String> keywords = Arrays.stream(keywordsField.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String servingSize = servingSizeField.getText().trim();

                    // Remove the old food
                    database.removeFood(food);

                    // Create a new composite food
                    CompositeFood updatedCompositeFood = new CompositeFood(name, keywords, servingSize);
                    components.forEach(updatedCompositeFood::addComponent);

                    return updatedCompositeFood;
                } catch (Exception e) {
                    showError("Error updating composite food: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<CompositeFood> result = dialog.showAndWait();

        result.ifPresent(updatedFood -> {
            database.addFood(updatedFood);
            refreshFoodList();
        });
    }


    private VBox createTopPanel() {
        /**
         * Creates the center panel containing the main food table view.
         *
         * @return VBox containing the center panel components
         */
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle(panelBackground);


        HBox searchBox = new HBox(15);
        searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Enter keywords to search items...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-font-size: 14px;");

        ToggleGroup searchToggle = new ToggleGroup();
        RadioButton matchAll = new RadioButton("Match All");
        RadioButton matchAny = new RadioButton("Match Any");
        matchAll.setToggleGroup(searchToggle);
        matchAny.setToggleGroup(searchToggle);
        matchAny.setSelected(true);

        Button searchButton = new Button("Search");
        searchButton.setStyle(buttonPrimary);
        searchButton.setPrefWidth(100);
        // Add listener to search field for real-time search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> keywords = Arrays.stream(newValue.split("\\s+"))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                boolean matchAllKeywords = matchAll.isSelected();
                List<Food> results = database.findFoodsByKeywords(keywords, matchAllKeywords);
                foodList.setAll(results);
            }
        });
        searchButton.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchText.isEmpty()) {
                refreshFoodList(); // Show all foods when search is empty
            } else {
                List<String> keywords = Arrays.asList(searchField.getText().split("\\s+"))
                        .stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
                boolean matchAllKeywords = matchAll.isSelected();
                List<Food> results = database.findFoodsByKeywords(keywords, matchAllKeywords);
                foodList.setAll(results);
            }
        });

        searchBox.getChildren().addAll(searchField, matchAll, matchAny, searchButton);

        // Control buttons
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button addBasicButton = new Button("Add Basic Food");
        addBasicButton.setStyle(buttonSuccess);

        Button addCompositeButton = new Button("Create Composite Food");
        addCompositeButton.setStyle(buttonSuccess);

        Button saveButton = new Button("Save Database");
        saveButton.setStyle(buttonWarning);

        // Button saveLogsButton = new Button("Save Logs");
        // saveLogsButton.setStyle(buttonWarning);

        addBasicButton.setOnAction(e -> showAddBasicFoodDialog());
        addCompositeButton.setOnAction(e -> showAddCompositeFoodDialog());

        saveButton.setOnAction(e -> {
            try {
                database.saveToFile();
                showInfo("Food database saved successfully!");
            } catch (IOException ex) {
                showError("Could not save food database: " + ex.getMessage());
            }
        });
        controlBox.getChildren().addAll(addBasicButton, addCompositeButton, saveButton);

        // Add sections to panel
        panel.getChildren().addAll(searchBox, controlBox);
        return panel;
    }

    private VBox createCenterPanel() {
        /**
         * Creates the right panel containing food details and user profile information.
         *
         * @return VBox containing the right panel components
         */
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle(panelBackground);

        Label titleLabel = new Label("Food Database");
        titleLabel.setStyle(titleStyle);

        foodList = FXCollections.observableArrayList();
        foodTable = new TableView<>(foodList);
        foodTable.setPlaceholder(new Label("No foods found - try adding some or changing your search"));
        foodTable.setStyle("-fx-font-size: 14px;");

        // Configure table columns with better styling
        TableColumn<Food, String> nameColumn = new TableColumn<>("Food Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId()));
        nameColumn.setPrefWidth(250);
        nameColumn.setStyle("-fx-font-weight: bold;");

        TableColumn<Food, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof BasicFood ? "Basic" : "Composite"));
        typeColumn.setPrefWidth(100);
        typeColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<Food, String> keywordsColumn = new TableColumn<>("Keywords");
        keywordsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().getKeywords())));
        keywordsColumn.setPrefWidth(250);

        TableColumn<Food, Number> caloriesColumn = new TableColumn<>("Calories/Serving");
        caloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCaloriesPerServing()));
        caloriesColumn.setPrefWidth(150);
        caloriesColumn.setStyle("-fx-alignment: CENTER;");

        foodTable.getColumns().addAll(nameColumn, typeColumn, keywordsColumn, caloriesColumn);

        foodTable.setRowFactory(tv -> {
            TableRow<Food> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Food selectedFood = row.getItem();
                    showFoodDetails(selectedFood);
                }
            });

            // Add hover effect
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #50bdf1;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });

        Button refreshButton = new Button("Refresh List");
        refreshButton.setStyle(buttonPrimary);
        refreshButton.setOnAction(e -> refreshFoodList());

        VBox.setVgrow(foodTable, Priority.ALWAYS);
        panel.getChildren().addAll(titleLabel, foodTable, refreshButton);

        return panel;
    }

    private DatePicker datePicker;
    private Label dateLabel;
    private TextArea profileArea;
    private TextArea calorieArea;

    private VBox createRightPanel() {
        /**
         * Updates the profile display with current user data.
         *
         * @param profileArea the TextArea to display profile information
         * @param calorieArea the TextArea to display calorie information
         * @param date the date to display information for
         */
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle(panelBackground);
        panel.setPrefWidth(350);

        // ==================== Food Details Section ====================
        Label foodDetailsLabel = new Label("Food Details");
        foodDetailsLabel.setStyle(titleStyle);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(300);
        detailsArea.setWrapText(true);
        detailsArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'ui-sans-serif';");

        HBox foodButtonBox = new HBox(10);
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle(buttonDanger);

        Button editButton = new Button("Edit");
        editButton.setStyle(buttonPrimary);
        editButton.setDisable(true);

        Button addToLogButton = new Button("Add to Log");
        addToLogButton.setStyle(buttonSuccess);
        addToLogButton.setDisable(true);

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setStyle(buttonPrimary);
        viewLogsButton.setOnAction(e -> showDailyLogEntries(datePicker.getValue()));

        foodButtonBox.getChildren().addAll(deleteButton, editButton, addToLogButton);

        // Food selection listener
        foodTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        StringBuilder details = new StringBuilder();
                        details.append("Name: ").append(newSelection.getId()).append("\n");
                        details.append("Type: ").append(newSelection instanceof BasicFood ? "Basic Food" : "Composite Food").append("\n");
                        details.append("Keywords: ").append(String.join(", ", newSelection.getKeywords())).append("\n\n");
                        details.append("Nutritional Information:\n");
                        details.append(String.format("Calories per Serving: %.2f\n", newSelection.getCaloriesPerServing()));

                        if (newSelection instanceof CompositeFood) {
                            CompositeFood composite = (CompositeFood) newSelection;
                            details.append("\nComposite Food Components:\n");
                            for (Map.Entry<Food, Double> component : composite.getComponents().entrySet()) {
                                details.append(String.format("- %s (%.2f servings)\n",
                                        component.getKey().getId(),
                                        component.getValue()));
                            }
                        }

                        detailsArea.setText(details.toString());
                        editButton.setDisable(false);
                        addToLogButton.setDisable(false);
                    } else {
                        detailsArea.setText("");
                        editButton.setDisable(true);
                        addToLogButton.setDisable(true);
                    }
                }
        );

        deleteButton.setOnAction(e -> {
            Food selectedFood = foodTable.getSelectionModel().getSelectedItem();
            if (selectedFood != null && confirmDialog("Delete Food", "Are you sure you want to delete '" + selectedFood.getId() + "'?")) {
                database.removeFood(selectedFood);
                refreshFoodList();
            }
        });

        editButton.setOnAction(e -> {
            Food selectedFood = foodTable.getSelectionModel().getSelectedItem();
            if (selectedFood != null) {
                if (selectedFood instanceof BasicFood) {
                    showEditBasicFoodDialog((BasicFood) selectedFood);
                } else if (selectedFood instanceof CompositeFood) {
                    showEditCompositeFoodDialog((CompositeFood) selectedFood);
                }
            }
        });


        addToLogButton.setOnAction(e -> {
            Food selectedFood = foodTable.getSelectionModel().getSelectedItem();
            if (selectedFood != null) {
                showAddToLogDialog(selectedFood);
            }
        });

        // ==================== User Profile Section ====================
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Date Selection
        dateLabel = new Label("Select Date:");
        dateLabel.setStyle(subtitleStyle);

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 14px;");
        datePicker.setOnAction(e -> refreshProfileDisplay());

        // Profile Display
        Label profileLabel = new Label("User Profile");
        profileLabel.setStyle(titleStyle);

        profileArea = new TextArea();
        profileArea.setEditable(false);
        profileArea.setPrefHeight(200);
        profileArea.setWrapText(true);
        profileArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'ui-sans-serif';");

        // Calorie Information
        Label calorieLabel = new Label("Daily Nutrition");
        calorieLabel.setStyle(titleStyle);

        calorieArea = new TextArea();
        calorieArea.setEditable(false);
        calorieArea.setPrefHeight(150);
        calorieArea.setWrapText(true);
        calorieArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'ui-sans-serif'; -fx-font-weight: bold;");

        // Update Profile Button
        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.setStyle(buttonPrimary);
        updateProfileButton.setOnAction(e -> showUpdateProfileDialog());

        // ==================== Add All Components ====================
        VBox foodDetailsSection = new VBox(10, foodDetailsLabel, detailsArea, foodButtonBox);
        VBox profileSection = new VBox(10,
                separator,
                dateLabel, datePicker,
                profileLabel, profileArea,
                calorieLabel, calorieArea,
                new HBox(10, updateProfileButton, viewLogsButton)
        );

        panel.getChildren().addAll(foodDetailsSection, profileSection);
        VBox.setVgrow(foodDetailsSection, Priority.ALWAYS);
        VBox.setVgrow(profileSection, Priority.ALWAYS);

        // Initialize profile display
        updateProfileDisplay(profileArea, calorieArea, LocalDate.now());

        return panel;
    }

    private void updateProfileDisplay(TextArea profileArea, TextArea calorieArea, LocalDate date) {
        /**
         * Shows a dialog for updating user profile information.
         */
        if (userProfile != null) {
            // Get or create the profile entry for the selected date
            UserProfile.ProfileEntry entry = userProfile.getProfileEntryForDate(date);

            StringBuilder profileInfo = new StringBuilder();
            profileInfo.append("Date: ").append(date).append("\n");
            profileInfo.append("Name: ").append(userProfile.getName()).append("\n");
            profileInfo.append("Gender: ").append(userProfile.getGender()).append("\n");
            profileInfo.append("Height: ").append(String.format("%.1f cm", userProfile.getHeight())).append("\n");
            profileInfo.append("Age: ").append(entry.getAge()).append("\n");
            profileInfo.append("Weight: ").append(String.format("%.1f kg", entry.getWeight())).append("\n");
            profileInfo.append("Activity Level: ").append(entry.getActivityLevel()).append("\n");
            profileInfo.append("Calculation Method: ").append(userProfile.getCalorieMethod()).append("\n");

            profileArea.setText(profileInfo.toString());

            // Calculate and display calorie information
            double targetCalories = entry.getTargetCalories();
            double consumedCalories = entry.getConsumedCalories();

            StringBuilder calorieText = new StringBuilder();
            calorieText.append("Target Calories: ").append(String.format("%.0f", targetCalories)).append("\n");
            calorieText.append("Consumed Calories: ").append(String.format("%.0f", consumedCalories)).append("\n");
            calorieText.append("Calorie Difference: ").append(String.format("%.0f",
                    consumedCalories - targetCalories)).append("\n");
            calorieText.append("(Using ").append(userProfile.getCalorieMethod()).append(" method)");

            calorieArea.setText(calorieText.toString());
        } else {
            profileArea.setText("No user profile available");
            calorieArea.setText("");
        }
    }

    private void showUpdateProfileDialog() {
        /**
         * Shows daily food log entries for a specific date.
         *
         * @param date the date to show entries for
         */
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Profile");
        dialog.setHeaderText("Update profile information for selected date");

        // Create dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker dialogDatePicker = new DatePicker(datePicker.getValue());
        grid.add(new Label("Date:"), 0, 0);
        grid.add(dialogDatePicker, 1, 0);

        UserProfile.ProfileEntry currentEntry = userProfile.getProfileEntryForDate(dialogDatePicker.getValue());

        TextField ageField = new TextField(String.valueOf(currentEntry.getAge()));
        ageField.setPromptText("Age in years");
        grid.add(new Label("Age:"), 0, 1);
        grid.add(ageField, 1, 1);

        TextField weightField = new TextField(String.valueOf(currentEntry.getWeight()));
        weightField.setPromptText("Weight in kg");
        grid.add(new Label("Weight:"), 0, 2);
        grid.add(weightField, 1, 2);

        ComboBox<UserProfile.ActivityLevel> activityLevelCombo = new ComboBox<>();
        activityLevelCombo.getItems().addAll(UserProfile.ActivityLevel.values());
        activityLevelCombo.setValue(currentEntry.getActivityLevel());
        grid.add(new Label("Activity Level:"), 0, 3);
        grid.add(activityLevelCombo, 1, 3);

        ComboBox<UserProfile.CalorieCalculationMethod> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll(UserProfile.CalorieCalculationMethod.values());
        methodCombo.setValue(userProfile.getCalorieMethod());
        grid.add(new Label("Calculation Method:"), 0, 4);
        grid.add(methodCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType submitButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    LocalDate updateDate = dialogDatePicker.getValue();
                    int age = Integer.parseInt(ageField.getText().trim());
                    double weight = Double.parseDouble(weightField.getText().trim());
                    UserProfile.ActivityLevel activityLevel = activityLevelCombo.getValue();
                    UserProfile.CalorieCalculationMethod method = methodCombo.getValue();

                    // Validate inputs
                    if (age <= 0 || age > 100) {
                        showError("Please enter a valid age");
                        return false;
                    }
                    if (weight <= 0) {
                        showError("Weight must be a positive number");
                        return false;
                    }
                    if (activityLevel == null) {
                        showError("Please select an activity level");
                        return false;
                    }
                    if (method == null) {
                        showError("Please select a calculation method");
                        return false;
                    }

                    userProfile.setCalorieMethod(method);
                    boolean updated = userProfile.updateProfileForDate(updateDate, age, weight, activityLevel);

                    if (!updated) {
                        showError("Failed to update profile");
                        return false;
                    }

                    try {
                        userProfile.saveToFile();
                        return true;
                    } catch (IOException e) {
                        showError("Failed to save profile: " + e.getMessage());
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showError("Please enter valid numeric values for age and weight");
                    return false;
                }
            }
            return false;
        });

        // Handle the dialog result
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(success -> {
            if (success) {
                refreshProfileDisplay();
            }
        });
    }

    private void showDailyLogEntries(LocalDate date) {
        /**
         * Shows a dialog for adding food to the daily log.
         *
         * @param food the Food object to add to the log
         */
        List<DailyLog.FoodEntry> entries = dailyLog.getFoodEntriesForDate(date);
        if (entries.isEmpty()) {
            showInfo("No food entries for " + date);
            //return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Daily Log - " + date);
        dialog.setHeaderText("Food consumed on " + date);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        TableView<DailyLog.FoodEntry> logTable = new TableView<>();
        ObservableList<DailyLog.FoodEntry> logEntries = FXCollections.observableArrayList(entries);

        // Food column
        TableColumn<DailyLog.FoodEntry, String> foodColumn = new TableColumn<>("Food");
        foodColumn.setCellValueFactory(cellData -> {
            Food food = database.getFoodById(cellData.getValue().getFoodId());
            return new SimpleStringProperty(food != null ? food.getId() : "Unknown Food");
        });

        // Servings column
        TableColumn<DailyLog.FoodEntry, Number> servingsColumn = new TableColumn<>("Servings");
        servingsColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getServings()));

        // Calories column
        TableColumn<DailyLog.FoodEntry, Number> caloriesColumn = new TableColumn<>("Total Calories");
        caloriesColumn.setCellValueFactory(cellData -> {
            Food food = database.getFoodById(cellData.getValue().getFoodId());
            return new SimpleDoubleProperty(food != null ?
                    food.getCaloriesPerServing() * cellData.getValue().getServings() : 0);
        });

        logTable.getColumns().addAll(foodColumn, servingsColumn, caloriesColumn);
        logTable.setItems(logEntries);

        // Delete button
        Button deleteButton = new Button("Delete Selected Entry");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setDisable(true);

        // Undo button
        Button undoButton = new Button("Undo");
        undoButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        undoButton.setDisable(!dailyLog.canUndo());

        // Enable/disable delete button based on selection
        logTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null);
        });

        deleteButton.setOnAction(e -> {
            DailyLog.FoodEntry selected = logTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Food food = database.getFoodById(selected.getFoodId());
                double caloriesToRemove = food != null ?
                        food.getCaloriesPerServing() * selected.getServings() : 0;

                if (dailyLog.removeFoodEntry(date, selected)) {
                    try {
                        dailyLog.saveToFile();
                        userProfile.recordConsumedCalories(date, -caloriesToRemove);
                        userProfile.saveToFile();
                        logEntries.remove(selected);
                        undoButton.setDisable(!dailyLog.canUndo());
                        refreshProfileDisplay();
                    } catch (IOException ex) {
                        showError("Error saving data: " + ex.getMessage());
                    }
                }
            }
        });

        undoButton.setOnAction(e -> {
            dailyLog.undoLastAction();
            try {
                // Refresh the log entries
                List<DailyLog.FoodEntry> updatedEntries = dailyLog.getFoodEntriesForDate(date);
                logEntries.setAll(updatedEntries);

                // Recalculate total calories
                double totalCalories = updatedEntries.stream()
                        .mapToDouble(entry -> {
                            Food food = database.getFoodById(entry.getFoodId());
                            return food != null ? food.getCaloriesPerServing() * entry.getServings() : 0;
                        })
                        .sum();

                // Update profile
                UserProfile.ProfileEntry profileEntry = userProfile.getProfileEntryForDate(date);
                profileEntry.setConsumedCalories(totalCalories);
                userProfile.saveToFile();

                refreshProfileDisplay();
                undoButton.setDisable(!dailyLog.canUndo());
            } catch (IOException ex) {
                showError("Error saving data: " + ex.getMessage());
            }
        });

        // Update UI when dialog is shown
        dialog.setOnShown(event -> {
            undoButton.setDisable(!dailyLog.canUndo());
        });

        HBox buttonBox = new HBox(10, deleteButton, undoButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox content = new VBox(10, logTable, buttonBox);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(450, 500);
        dialog.showAndWait();
    }

    private void showAddToLogDialog(Food food) {
        /**
         * Refreshes the profile display with current data.
         */
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Add to Daily Log");
        dialog.setHeaderText("Add " + food.getId() + " to your log");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker logDatePicker = new DatePicker(LocalDate.now());
        Spinner<Double> servingsSpinner = new Spinner<>(0.1, 10.0, 1.0, 0.1);
        servingsSpinner.setEditable(true);
        servingsSpinner.setPrefWidth(100);

        grid.add(new Label("Date:"), 0, 0);
        grid.add(logDatePicker, 1, 0);
        grid.add(new Label("Servings:"), 0, 1);
        grid.add(servingsSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("date", logDatePicker.getValue());
                result.put("servings", servingsSpinner.getValue());
                return result;
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            LocalDate date = (LocalDate) data.get("date");
            double servings = (Double) data.get("servings");

            dailyLog.addFoodEntry(food, servings, date);
            try {
                dailyLog.saveToFile();
                userProfile.recordConsumedCalories(date, food.getCaloriesPerServing() * servings);
                userProfile.saveToFile();
                refreshProfileDisplay();
                showInfo(food.getId() + " added to daily log for " + date);
            } catch (IOException ex) {
                showError("Error saving log: " + ex.getMessage());
            }
        });
    }

    private void refreshProfileDisplay() {
        /**
         * Refreshes the food list from the database.
         */

        if (profileArea != null && calorieArea != null) {
            updateProfileDisplay(profileArea, calorieArea, datePicker.getValue());
        }
    }

    private void refreshFoodList() {
        /**
         * Shows a dialog for adding a new basic food.
         */
        if (foodList == null) {
            foodList = FXCollections.observableArrayList();
        }
        foodList.setAll(database.getAllFoods());
    }

    private void showAddBasicFoodDialog() {
        /**
         * Shows a dialog for adding a new composite food.
         */
        Dialog<BasicFood> dialog = new Dialog<>();
        dialog.setTitle("Add Basic Food");
        dialog.setHeaderText("Enter basic food details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Food name");
        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma separated)");
        TextField servingSizeField = new TextField();
        servingSizeField.setPromptText("Serving size (e.g., '1 medium apple', '100g')");
        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Calories per serving");
        TextField proteinField = new TextField();
        proteinField.setPromptText("Protein (g)");
        TextField carbsField = new TextField();
        carbsField.setPromptText("Carbohydrates (g)");
        TextField fatsField = new TextField();
        fatsField.setPromptText("Fats (g)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Keywords:"), 0, 1);
        grid.add(keywordsField, 1, 1);
        grid.add(new Label("Serving Size:"), 0, 2);
        grid.add(servingSizeField, 1, 2);
        grid.add(new Label("Calories:"), 0, 3);
        grid.add(caloriesField, 1, 3);
        grid.add(new Label("Protein (g):"), 0, 4);
        grid.add(proteinField, 1, 4);
        grid.add(new Label("Carbs (g):"), 0, 5);
        grid.add(carbsField, 1, 5);
        grid.add(new Label("Fats (g):"), 0, 6);
        grid.add(fatsField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> nameField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    List<String> keywords = Arrays.stream(keywordsField.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String servingSize = servingSizeField.getText().trim();
                    double calories = Double.parseDouble(caloriesField.getText().trim());
                    double protein = Double.parseDouble(proteinField.getText().trim());
                    double carbs = Double.parseDouble(carbsField.getText().trim());
                    double fats = Double.parseDouble(fatsField.getText().trim());

                    return new BasicFood(name, keywords, servingSize, calories, protein, carbs, fats);
                } catch (NumberFormatException e) {
                    showError("Invalid nutritional values. Please enter numbers.");
                    return null;
                }
            }
            return null;
        });

        Optional<BasicFood> result = dialog.showAndWait();

        result.ifPresent(food -> {
            database.addFood(food);
            refreshFoodList();
        });
    }

    private void showAddCompositeFoodDialog() {
        /**
         * Shows detailed information about a food item.
         *
         * @param food the Food object to display
         */
        Dialog<CompositeFood> dialog = new Dialog<>();
        dialog.setTitle("Create Composite Food");
        dialog.setHeaderText("Enter composite food details:");

        ButtonType addButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Composite food name");
        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma separated)");
        TextField servingSizeField = new TextField();
        servingSizeField.setPromptText("Serving size (e.g., '1 sandwich', '200g')");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Keywords:"), 0, 1);
        grid.add(keywordsField, 1, 1);
        grid.add(new Label("Serving Size:"), 0, 2);
        grid.add(servingSizeField, 1, 2);

        content.getChildren().add(grid);

        // Component selection
        Label componentsLabel = new Label("Add Components:");
        componentsLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(componentsLabel);

        TableView<Food> foodSelectionTable = new TableView<>();
        ObservableList<Food> availableFoods = FXCollections.observableArrayList(database.getAllFoods());

        TableColumn<Food, String> nameColumn = new TableColumn<>("Food Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Food, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof BasicFood ? "Basic" : "Composite"));

        TableColumn<Food, Number> caloriesColumn = new TableColumn<>("Calories");
        caloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCaloriesPerServing()));

        foodSelectionTable.getColumns().addAll(nameColumn, typeColumn, caloriesColumn);
        foodSelectionTable.setItems(availableFoods);

        HBox servingBox = new HBox(10);
        servingBox.setPadding(new Insets(5));

        Spinner<Double> servingsSpinner = new Spinner<>(0.1, 10.0, 1.0, 0.1);
        servingsSpinner.setEditable(true);
        servingsSpinner.setPrefWidth(100);

        Button addComponentButton = new Button("Add Component");
        addComponentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        servingBox.getChildren().addAll(new Label("Servings:"), servingsSpinner, addComponentButton);

        // Component list
        Label selectedComponentsLabel = new Label("Selected Components:");
        selectedComponentsLabel.setStyle("-fx-font-weight: bold;");

        TableView<Map.Entry<Food, Double>> componentsTable = new TableView<>();
        ObservableList<Map.Entry<Food, Double>> selectedComponents = FXCollections.observableArrayList();

        TableColumn<Map.Entry<Food, Double>, String> componentNameColumn = new TableColumn<>("Food");
        componentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getId()));

        TableColumn<Map.Entry<Food, Double>, Number> servingsColumn = new TableColumn<>("Servings");
        servingsColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getValue()));

        TableColumn<Map.Entry<Food, Double>, Number> totalCaloriesColumn = new TableColumn<>("Total Calories");
        totalCaloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(
                        cellData.getValue().getKey().getCaloriesPerServing() * cellData.getValue().getValue()));

        TableColumn<Map.Entry<Food, Double>, String> servingSizeColumn = new TableColumn<>("Serving Size");
        servingSizeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getServingSize()));
        // Update column widths
        componentNameColumn.setPrefWidth(200);  // Increased width for food name
        servingSizeColumn.setPrefWidth(150);    // Moderate width for serving size
        servingsColumn.setPrefWidth(100);       // Narrow width for servings
        totalCaloriesColumn.setPrefWidth(120);  // Moderate width for total calories

        // Optional: Make the table horizontally scrollable
        componentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        componentsTable.getColumns().addAll(
                componentNameColumn,
                servingSizeColumn,  // Add the new serving size column
                servingsColumn,
                totalCaloriesColumn
        );        componentsTable.setItems(selectedComponents);

        Button removeComponentButton = new Button("Remove Selected Component");
        removeComponentButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

        // Component selection handling
        final Map<Food, Double> components = new HashMap<>();

        addComponentButton.setOnAction(e -> {
            Food selectedFood = foodSelectionTable.getSelectionModel().getSelectedItem();
            if (selectedFood != null) {
                double servings = servingsSpinner.getValue();
                components.put(selectedFood, components.getOrDefault(selectedFood, 0.0) + servings);

                // Refresh the components table
                selectedComponents.clear();
                components.entrySet().forEach(selectedComponents::add);
            } else {
                showWarning("Please select a food to add.");
            }
        });

        removeComponentButton.setOnAction(e -> {
            Map.Entry<Food, Double> selectedEntry = componentsTable.getSelectionModel().getSelectedItem();
            if (selectedEntry != null) {
                components.remove(selectedEntry.getKey());

                // Refresh the components table
                selectedComponents.clear();
                components.entrySet().forEach(selectedComponents::add);
            } else {
                showWarning("Please select a component to remove.");
            }
        });

        content.getChildren().addAll(
                foodSelectionTable, servingBox,
                selectedComponentsLabel, componentsTable, removeComponentButton
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(700);

        Platform.runLater(() -> nameField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    List<String> keywords = Arrays.stream(keywordsField.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String servingSize = servingSizeField.getText().trim();

                    CompositeFood compositeFood = new CompositeFood(name, keywords, servingSize);
                    components.forEach(compositeFood::addComponent);

                    return compositeFood;
                } catch (Exception e) {
                    showError("Error creating composite food: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<CompositeFood> result = dialog.showAndWait();

        result.ifPresent(food -> {
            database.addFood(food);
            refreshFoodList();
        });
    }

    private void showFoodDetails(Food food) {
        /**
         * Shows detailed information about a basic food item.
         *
         * @param food the BasicFood object to display
         */
        if (food instanceof BasicFood) {
            showBasicFoodDetails((BasicFood) food);
        } else if (food instanceof CompositeFood) {
            showCompositeFoodDetails((CompositeFood) food);
        }
    }

    private void showBasicFoodDetails(BasicFood food) {
        /**
         * Shows detailed information about a composite food item.
         *
         * @param food the CompositeFood object to display
         */
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Food Details");
        dialog.setHeaderText(food.getId() + " Details");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(food.getId()), 1, 0);

        grid.add(new Label("Type:"), 0, 1);
        grid.add(new Label("Basic Food"), 1, 1);

        grid.add(new Label("Keywords:"), 0, 2);
        grid.add(new Label(String.join(", ", food.getKeywords())), 1, 2);

        grid.add(new Label("Serving Size:"), 0, 3);
        grid.add(new Label(food.getServingSize()), 1, 3);

        grid.add(new Label("Calories per Serving:"), 0, 4);
        grid.add(new Label(String.format("%.2f", food.getCaloriesPerServing())), 1, 4);

        grid.add(new Label("Nutritional Information:"), 0, 5);
        grid.add(new Label(String.format("Protein: %.1fg | Carbs: %.1fg | Fats: %.1fg",
                food.getProtein(), food.getCarbs(), food.getFats())), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();
    }


    private void showCompositeFoodDetails(CompositeFood food) {
        /**
         * Loads initial data from files or creates sample data if files don't exist.
         */
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Composite Food Details");
        dialog.setHeaderText(food.getId() + " Details");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 10, 10, 10));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(food.getId()), 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(new Label("Composite Food"), 1, 1);

        grid.add(new Label("Keywords:"), 0, 2);
        grid.add(new Label(String.join(", ", food.getKeywords())), 1, 2);

        grid.add(new Label("Serving Size:"), 0, 3);
        grid.add(new Label(food.getServingSize()), 1, 3);

        grid.add(new Label("Calories per Serving:"), 0, 3);
        grid.add(new Label(String.format("%.2f", food.getCaloriesPerServing())), 1, 3);

        grid.add(new Label("Nutritional Information:"), 0, 5);
        grid.add(new Label(String.format("Protein: %.1fg | Carbs: %.1fg | Fats: %.1fg",
                food.getProtein(), food.getCarbs(), food.getFats())), 1, 5);


        content.getChildren().add(grid);

        Label componentsLabel = new Label("Components:");
        componentsLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(componentsLabel);

        TableView<Map.Entry<Food, Double>> componentsTable = new TableView<>();
        ObservableList<Map.Entry<Food, Double>> components =
                FXCollections.observableArrayList(food.getComponents().entrySet());

        TableColumn<Map.Entry<Food, Double>, String> nameColumn = new TableColumn<>("Food Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey().getId()));
        nameColumn.setPrefWidth(200);

        TableColumn<Map.Entry<Food, Double>, Number> servingsColumn = new TableColumn<>("Servings");
        servingsColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getValue()));
        servingsColumn.setPrefWidth(100);

        TableColumn<Map.Entry<Food, Double>, Number> caloriesColumn = new TableColumn<>("Calories Contribution");
        caloriesColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getKey().getCaloriesPerServing()
                        * cellData.getValue().getValue()));
        caloriesColumn.setPrefWidth(150);

        componentsTable.getColumns().addAll(nameColumn, servingsColumn, caloriesColumn);
        componentsTable.setItems(components);

        content.getChildren().add(componentsTable);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(400);

        dialog.showAndWait();
    }

    private void loadInitialData() {
        /**
         * Creates sample food data for initial application use.
         */
        try {
            database.loadFromFile();
            refreshFoodList();
        } catch (IOException e) {
            // If file doesn't exist, create some sample data
            createSampleData();
            showInfo("Created sample food database. You can add more foods as needed.");
        }

        // Load user profile
        try {
            userProfile = UserProfile.loadFromFile();
            // Initialize today's entry if profile exists but no entry for today
            if (userProfile != null) {
                // Calculate consumed calories from logs for all dates
                Map<LocalDate, List<DailyLog.FoodEntry>> allEntries = dailyLog.getAllEntries();
                for (Map.Entry<LocalDate, List<DailyLog.FoodEntry>> entry : allEntries.entrySet()) {
                    LocalDate date = entry.getKey();
                    double totalCalories = entry.getValue().stream()
                            .mapToDouble(foodEntry -> {
                                Food food = database.getFoodById(foodEntry.getFoodId());
                                return food != null ? food.getCaloriesPerServing() * foodEntry.getServings() : 0;
                            })
                            .sum();
                    userProfile.recordConsumedCalories(date, totalCalories);
                }
                // Ensure today's entry exists
                userProfile.getProfileEntryForDate(LocalDate.now());
            }
        } catch (IOException e) {
            showError("Failed to load user profile: " + e.getMessage());
            userProfile = null;
        }
    }

    private void createSampleData() {
        /**
         * Shows an error alert dialog.
         *
         * @param message the error message to display
         */

        // Basic Foods
        database.addFood(new BasicFood("Apple", Arrays.asList("fruit", "sweet", "snack"), "1 medium (182g)", 95, 0.5, 25, 0.3));
        database.addFood(new BasicFood("Banana", Arrays.asList("fruit", "sweet", "snack"), "1 medium (118g)", 105, 1.3, 27, 0.4));
        database.addFood(new BasicFood("Chicken Breast", Arrays.asList("meat", "protein", "dinner"), "1 breast (172g)", 165, 31, 0, 3.6));
        database.addFood(new BasicFood("White Rice", Arrays.asList("grain", "carb", "dinner"), "1 cup cooked (186g)", 200, 4, 45, 0.4));
        database.addFood(new BasicFood("Whole Wheat Bread", Arrays.asList("grain", "carb", "bread", "breakfast"), "1 slice (28g)", 80, 4, 12, 1.1));
        database.addFood(new BasicFood("Peanut Butter", Arrays.asList("protein", "spread", "breakfast"), "2 tbsp (32g)", 190, 8, 6, 16));
        database.addFood(new BasicFood("Milk 1%", Arrays.asList("dairy", "drink", "breakfast"), "1 cup (244g)", 102, 8, 12, 2.4));
        database.addFood(new BasicFood("Egg", Arrays.asList("protein", "breakfast"), "1 large (50g)", 70, 6, 0.6, 5));
        database.addFood(new BasicFood("Cheddar Cheese", Arrays.asList("dairy", "protein", "snack"), "1 oz (28g)", 113, 7, 0.4, 9.4));
        database.addFood(new BasicFood("Spinach", Arrays.asList("vegetable", "greens", "dinner"), "1 cup raw (30g)", 7, 0.9, 1.1, 0.1));
        database.addFood(new BasicFood("Olive Oil", Arrays.asList("oil", "fat", "cooking"), "1 tbsp (14g)", 119, 0, 0, 14));
        database.addFood(new BasicFood("Salmon", Arrays.asList("fish", "protein", "dinner"), "3 oz (85g)", 206, 22, 0, 13));

        // Create some composite foods
        CompositeFood pbSandwich = new CompositeFood("Peanut Butter Sandwich",
                Arrays.asList("sandwich", "lunch", "peanut butter"),
                "2 slices bread, 1 serving peanut butter");
        pbSandwich.addComponent(database.getFoodById("Whole Wheat Bread"), 2.0); // 2 slices
        pbSandwich.addComponent(database.getFoodById("Peanut Butter"), 1.0); // 1 serving
        database.addFood(pbSandwich);

        CompositeFood scrambledEggs = new CompositeFood("Scrambled Eggs with Cheese",
                Arrays.asList("breakfast", "eggs", "protein"),
                "2 eggs, 1/2 serving cheese, 1/4 serving olive oil");
        scrambledEggs.addComponent(database.getFoodById("Egg"), 2.0); // 2 eggs
        scrambledEggs.addComponent(database.getFoodById("Cheddar Cheese"), 0.5); // 1/2 serving
        scrambledEggs.addComponent(database.getFoodById("Olive Oil"), 0.25); // 1/4 serving
        database.addFood(scrambledEggs);

        // Save the sample data
        try {
            database.saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        refreshFoodList();
    }

    private void showError(String message) {
        /**
         * Shows an error alert dialog.
         *
         * @param message the error message to display
         */

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        /**
         * Shows an information alert dialog.
         *
         * @param message the information message to display
         */
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        /**
         * Shows a confirmation dialog.
         *
         * @param title the dialog title
         * @param message the confirmation message
         * @return true if user confirmed, false otherwise
         */
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmDialog(String title, String message) {
        /**
         * Main entry point for the application.
         *
         * @param args command line arguments
         */
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}