# YADA (Yet Another Diet Assistant)

**A JavaFX-based application to help users manage food intake and track daily nutrition.**

---

## Running the Application

### Prerequisites
- Java 24
- JavaFX 24
- Recommended IDE: IntelliJ IDEA

### Installation & Setup

1. **Clone the repository:**
git clone https://github.com/akshitha-gayatri/Diet_Manager
or [Download ZIP](https://github.com/akshitha-gayatri/Diet_Manager)

2. **Open in IntelliJ IDEA**

3. **Set up JavaFX:**
- Go to `File > Project Structure > Libraries` and add the JavaFX SDK (`javafx-sdk-24/lib`)
- Add VM options to the run configuration:
  ```
  --module-path "path/to/javafx-sdk-24/lib" --add-modules javafx.controls,javafx.fxml
  ```

4. **Run the Application:**
- Run `YADAApplication.java`

---

## Key Features

### A. Food Database

- **Add Basic Food:**
- Use the *Add Basic Food* button
- Example: `Name = Apple`, `Keywords = fruit,snack`, `Calories = 95`

- **Create Composite Food:**
- Use *Create Composite Food* to combine items
- Example: `2x Bread + 1x Peanut Butter = PB Sandwich`

- **Manage Foods:**
- Edit/Delete via right-panel (double-click shows nutritional details)
- Foods can be sorted by column headers (ascending/descending)
- Use *Save to Database* to persist changes
- Use *Refresh List* to revert to original view

### B. Daily Logging by Date

1. Select a food item from the table
2. Click *Add to Log*
3. Enter servings and date (e.g., `1.5`, `2025-05-22`)
4. View entries with the *View Logs* button
5. Delete an entry and test *Undo* functionality

### C. Profile Management (Single User)

- Setup: Enter user details (name, gender, height, etc.)
- Update metrics using calendar-based view
- Calorie balance shown in right-hand panel

### D. Search Functionality

- Search foods using keywords
- **Match Any** or **Match All** options
- Filtering updates dynamically as you type

---

## Sample Data

Pre-loaded food items:

- **Basic Foods:** Apple, Chicken Breast, Milk
- **Composite Foods:** PB Sandwich, Scrambled Eggs

Try logging `2 servings of PB Sandwich` to see calorie tracking in action.

---

## Data Files

- `foods.txt` – Contains food database entries
- `daily_logs.txt` – Logs daily consumption
- `user_profile.txt` – Stores user profile details

---

## Troubleshooting

- **JavaFX Errors:** Ensure correct SDK path in VM options
- **File Write Errors:** Check file/folder permissions
- **Blank UI Screen:** Watch console for exception messages

---

## Repository

GitHub Repo: [YADA on GitHub](https://github.com/akshitha-gayatri/Diet_Manager)

