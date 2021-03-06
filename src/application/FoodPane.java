package application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

/**
 * This class handles the bulk of operations dealing with the displayed
 * list of foods. It creates a scrollable list of food items with a search bar
 * where the user may search the foods by name, a counter which shows the number
 * of foods available in the list, and multiple buttons. These buttons allow the user
 * to load new foods into the list from a file, add individual, user created new foods to the list, 
 * save the current foods displayed to a file, and add selected foods to the Meal List.
 
 * 
 *@authors d-team 57 
 */
public class FoodPane extends BorderPane{
	private GUIManager guiManager; // reference to guiManager
    private TableView foodTable = new TableView(); // table to display food options
    private MealListPane mlp; // reference to meal list pane
    private FoodList foodList = new FoodList(); // list of food options
    private ArrayList<Food> foodArrList = new ArrayList<Food>();// list of currently displayed foods
    TextField filterField; // for filtering based on nutrients
    private Label foodLabel; // "Food List" label
    private Label foodCount; // shows number of foods diplayed in list
    private HBox headerFPane; // container for Food Pane header
    private Button addFoodToMealBtn; // button for adding food to meal
    private Button addSingleFoodBtn; //button for adding only one item to food list
    private Button loadAddtnlFoodBtn; //button for loading file to food list
    private Button helpBtn; //button for help for user
    private Button saveFoodsBtn;//button for saving food to a file
    private VBox foodPane; //container for headerFPane, filterField, food table, and button grid 
    private TableColumn nameCol; // visibility for filtering alphabetically based on names
    Set<String> idSet = new HashSet<String>(); // keeps track of unique ids of all foods in list

    /**
     * Constructs a FoodPane containing information
     * about the name and nutritional content of various foods.
     * @param mlp A reference to the MealListPane that will be fed data from the FoodPane.
     * @author d-team 57
     */
    public FoodPane(){
    }
    
    //separated constructor from creation because now we have access to Scene
    public void create(MealListPane mlp, GUIManager guiManager) {
        try {
        	this.guiManager =  guiManager;
            this.mlp = mlp;
            this.setRight(foodPane());
            this.requestFocus();//removes focus from filterField
            this.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), ()->this.requestFocus());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Returns a VBox containing the TableView displaying
     * the food data. 
     * @return VBox of TableView displaying the food data
     */
    public VBox foodPane() {
        this.foodLabel = new Label("Food List (empty):");
        this.headerFPane = new HBox();
        this.headerFPane.getChildren().addAll(foodLabel);

        foodLabel.setId("section-heading");
        this.filterField = new TextField();
        this.filterField.setPromptText("filter by name");
       
        // Arranging buttons
        GridPane buttonGrid = new GridPane();
        this.addFoodToMealBtn = new Button("Add to meal (a)");
        this.addSingleFoodBtn = new Button("Add to list (n)");
        this.loadAddtnlFoodBtn = new Button("Load (ctrl-o)");
        this.helpBtn = new Button("Help!");
        Region padding = new Region();
        padding.setPrefWidth(400);
        HBox.setHgrow(padding, Priority.ALWAYS);
        this.saveFoodsBtn = new Button("Save (ctrl-s)");
        buttonGrid.add(this.addSingleFoodBtn, 0, 0, 2, 1);
        buttonGrid.add(this.loadAddtnlFoodBtn, 0, 2);
        buttonGrid.add(this.saveFoodsBtn, 1, 2);
        buttonGrid.add(padding, 1, 0);
        buttonGrid.add(this.addFoodToMealBtn, 3, 0);
        buttonGrid.add(this.helpBtn, 3, 2);

        // name the columns
        setupColumns();

        // format table size and enable selection of multiple foods at once
        this.foodTable.setPrefWidth(500);
        this.foodTable.setPrefHeight(500);
        this.foodTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set up and display scene
        this.foodPane = new VBox(10);
        this.foodPane.setId("food-data");
        this.foodPane.setPadding(new Insets(20, 20, 20, 20));
        this.foodPane.getChildren().addAll(this.headerFPane, this.filterField, this.foodTable, buttonGrid);

        ///////////////////////////
        // Button Event Handling //
        ///////////////////////////

        // when add food to meal is pressed, add selected food item(s) to meal list
        addFoodToMealBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ObservableList<Food> selected = foodTable.getSelectionModel().getSelectedItems();
                ArrayList<Food> selectedArr = new ArrayList<Food>();

                for (Food f: selected) {
                    selectedArr.add(f);
                }

                updateMealListPane(selectedArr);
            }
        });
        KeyCodeCombination addToMeal = new KeyCodeCombination(KeyCode.A);
        this.getScene().getAccelerators().put(addToMeal, ()->addFoodToMealBtn.fire());
        

        // when add food to list is pressed, deploy form and add food to list
        addSingleFoodBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                addSingularFood();
            }
        });
        KeyCodeCombination addToList = new KeyCodeCombination(KeyCode.N);
        this.getScene().getAccelerators().put(addToList, ()->addSingleFoodBtn.fire());

        // when load new food list from file is pressed, deploy form and load new data into list
        loadAddtnlFoodBtn.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
                loadNewFoodFile();
            }
        });
        KeyCodeCombination loadFromFile = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN);
        this.getScene().getAccelerators().put(loadFromFile, ()->loadAddtnlFoodBtn.fire());

        // when load save food list button is pressed, deploy form for user to name the file then export the data
        saveFoodsBtn.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
                saveFoodToFile();
            }
        });
        KeyCodeCombination saveToFile = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        this.getScene().getAccelerators().put(saveToFile, ()->saveFoodsBtn.fire());

        // when help button pressed, open up a text dialog that tells them how to select more than one food
        helpBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Here's some info: ");
                alert.setHeaderText("");
                alert.setContentText("-To load a new csv file, click \"Load\"."
                		             + "Don't forget to put your file in the same folder as the .jar file!\n"
                		             + "\n\n-To save your current foods, click \"Save\"."
                		             + " Note that only the foods currenlty being displayed will be saved."
                		             + "\n\n-To add foods to a meal, first select the foods from the list of foods then press \"Add to meal\""
                		             + "\n\n-Similarly, to remove, highlight the foods in your meal then press remove."
                		             + ". Note that you can only select one food at a time from your meal.");
                alert.showAndWait();
            } 
        });
        return foodPane;
    }

    /*
     * Deploys a form to get user input about a food and then
     * adds that food to the food list.
     */
    public void addSingularFood() {
        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        //Defining the Name text field
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setPrefColumnCount(10);
        GridPane.setConstraints(nameField, 0, 0);
        nameField.setId("focused");

        //Defining the Calories text field
        TextField calsField = new TextField();
        calsField.setPromptText("Calories");
        GridPane.setConstraints(calsField, 0, 1);

        //Defining the Fat text field
        TextField fatField = new TextField();
        fatField.setPromptText("Fat");
        GridPane.setConstraints(fatField, 0, 2);

        //Defining the Carbs text field
        TextField carbsField = new TextField();
        carbsField.setPromptText("Carbs");
        GridPane.setConstraints(carbsField, 0, 3);

        //Defining the Fiber text field
        TextField fiberField = new TextField();
        fiberField.setPromptText("Fiber");
        GridPane.setConstraints(fiberField, 0, 4);

        //Defining the Protein text field
        TextField proteinField = new TextField();
        proteinField.setPromptText("Protein");
        GridPane.setConstraints(proteinField, 0, 5);

        grid.getChildren().addAll(nameField, calsField, fatField, carbsField, fiberField, proteinField);
        Dialog<Food> dialog = new Dialog<Food>();
        dialog.setTitle("Add Food");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookup("#focused").requestFocus();

        // get user input and add food to table
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                String id = "invalidID";
                String name = "invalidName";
                double calories = -1;
                double fat = -1;
                double carbs = -1;
                double fiber = -1;
                double protein = -1;

                try {
                    name = nameField.getText();
                    calories = Double.parseDouble(calsField.getText());
                    fat = Double.parseDouble(fatField.getText());
                    carbs = Double.parseDouble(carbsField.getText());
                    fiber = Double.parseDouble(fiberField.getText());
                    protein = Double.parseDouble(proteinField.getText());

                    // verify that all nutrient fields are non negative
                    if (calories < 0 || fat < 0 || carbs < 0 || fiber < 0 || protein < 0) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Negative Value(s) Not Permitted");
                        alert.setHeaderText(null);
                        alert.setContentText("Negative values for nutrient fields are not permitted.");
                        alert.showAndWait();
                        return null;
                    }
                } catch (NumberFormatException e) {
                    // tell user that error occurred and food was not added
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occured while adding single food to food list.");
                    alert.showAndWait();
                    return null;
                }

                return new Food(name, calories, fat, carbs, fiber, protein);

            }
            return null;
        });

        // add food to food list if id is unique
        Optional<Food> foodData = dialog.showAndWait();
        
        if (foodData.isPresent()) {
            if (!this.idSet.contains(foodData.get().getID())) {
          	    this.guiManager.addFood(foodData.get());
          	    this.idSet.add(foodData.get().getID()); // add id to set
                dialog.close();
            } else {
             // tell user that duplicate records cannot be added
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Duplicate Food");
                alert.setHeaderText(null);
                alert.setContentText("This food is already in the list. Duplicate records cannot be added.");
                alert.showAndWait();
            }
        }   
    }

    /*
     * Deploys form to get path to food file from the user
     * and then loads those foods into the food list.
     */
    public void loadNewFoodFile() {
        TextInputDialog dialog = new TextInputDialog("ex: User/Desktop/FoodList.csv");
        dialog.setTitle("Meal Planner");
        dialog.setGraphic(null);
        dialog.setHeaderText("Enter path to Food Data");
        dialog.setContentText("Path:");
        dialog.getDialogPane().setMinWidth(500);

        // Get filepath from user and load data into Food List
        Optional<String> path = dialog.showAndWait();
        if (path.isPresent()) {
            IOHandler csvReader = new IOHandler();
            this.guiManager.resetFoodPane(csvReader.read(path.get()));
            csvReader.shareIDSet(this); // update set of unique food ids in FoodPane instance
            dialog.close();
        }
    }

    /*
     * Deploys form to get name of food file from the user
     * and then saves the currently filtered foods to that file.
     */
    public void saveFoodToFile() {
        TextInputDialog dialog = new TextInputDialog("ex: FoodList");
        dialog.setTitle("Meal Planner");
        dialog.setGraphic(null);
        dialog.setHeaderText("Enter name of file to be created.");
        dialog.setContentText("Name:");
        dialog.getDialogPane().setMinWidth(500);

        // Get file name from user and load current filtered foods into new file
        Optional<String> fileName = dialog.showAndWait();
        if (fileName.isPresent()) {
            IOHandler csvWriter = new IOHandler();
            csvWriter.write(fileName.get(), foodArrList);
            dialog.close();
        }
    }
    
    /*
     * Loads the meal pane with the currently selected foods
     * in the food pane. 
     * 
     * @param selectedArr The currently selected foods.
     */
    protected void updateMealListPane(ArrayList<Food> selectedArr) {
        this.mlp.updateMealListPane(selectedArr);       
    }
    
    /*
     * Replaces the current ArrayList of foods with the
     * new ArrayList passed in. 
     * 
     * @param foodArrList The new ArrayList of foods.
     * 
     */
    protected void updateFoodArrList(ArrayList<Food> foodArrList){
    	this.foodArrList=foodArrList;
		updateFoodPane();
    }
    

    /*
     * Takes in an ArrayList of FoodItems and fills the Table
     * with those foods.
     * @param food The ArrayList of FoodItems
     */
    //TODO: maybe make foodObsList a class variable? I think to add new elements, you'll need to access it
    //Or maybe filteredData?

    public void updateFoodPane() {
        //See https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        this.foodArrList.sort((a, b)->{return a.getName().compareTo(b.getName());});
        FilteredList<Food> filteredData = new FilteredList<Food>(FXCollections.observableList(foodArrList), p->true);

        this.filterField.textProperty().addListener((observable, oldVal, newVal) -> {
            //I think the predicate determines what's shown and what isn't
            filteredData.setPredicate( target -> {
                //Empty filter, show everything
                if (newVal==null || newVal.isEmpty()){
                    return true;
                }

                String input = newVal.toLowerCase();//filter isn't case sensitive
                if(target.getName().toLowerCase().contains(input)){
                    return true;
                } else{
                    return false;
                }

            });
        });
        
        // reset food list to display filtered data
        SortedList<Food> sortedData = new SortedList<Food>(filteredData);
        //tbh, not sure what's the diff between Observable, Filtered, and Sorted list.
        sortedData.comparatorProperty().bind(this.foodTable.comparatorProperty());
        this.foodTable.setItems(sortedData); 
//        foodTable.getSortOrder().add(nameCol);
        // reset count of foods in the list
        this.foodLabel = new Label("Food List (" + (foodArrList.size()==0?"empty":foodArrList.size()) + "):");
        this.foodLabel.setId("section-heading");
//        this.foodCount = new Label("Number of Food Items: " + foodArrList.size());
        this.headerFPane.getChildren().clear();
        this.headerFPane.getChildren().addAll(foodLabel);
        
    }


    /*
     * Sets up the columns in the food table.
     * Sets up Name, Calories, Fat, Carbs, Fiber, and Protein
     * columns in that order.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupColumns() {
        // name
        nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory("Name"));

        // calories
        TableColumn caloriesCol = new TableColumn("Calories");
        caloriesCol.setCellValueFactory(new PropertyValueFactory("Calories"));

        // fat
        TableColumn fatCol = new TableColumn("Fat");
        fatCol.setCellValueFactory(new PropertyValueFactory("Fat"));

        // carbs
        TableColumn carbsCol = new TableColumn("Carbs");
        carbsCol.setCellValueFactory(new PropertyValueFactory("Carbs"));

        // fiber
        TableColumn fiberCol = new TableColumn("Fiber");
        fiberCol.setCellValueFactory(new PropertyValueFactory("Fiber"));

        // protein
        TableColumn proteinCol = new TableColumn("Protein");
        proteinCol.setCellValueFactory(new PropertyValueFactory("Protein"));

        // add all columns to table
        this.foodTable.getColumns().setAll(nameCol, caloriesCol, fatCol, carbsCol, fiberCol, proteinCol);
        nameCol.prefWidthProperty().bind(foodTable.widthProperty().divide(2.4));
        caloriesCol.prefWidthProperty().bind(foodTable.widthProperty().divide(7));
        fatCol.prefWidthProperty().bind(foodTable.widthProperty().divide(11.0));
        carbsCol.prefWidthProperty().bind(foodTable.widthProperty().divide(9.0));
        fiberCol.prefWidthProperty().bind(foodTable.widthProperty().divide(10.0));
        proteinCol.prefWidthProperty().bind(foodTable.widthProperty().divide(7.0));
        
        // set the height of Food Table to a ratio of the screen's height
        this.foodTable.setMinHeight((0.60) * Screen.getPrimary().getBounds().getHeight());
    }
    
    /*
     * Replaces the current set of unique ids
     * with the set passed in.
     * 
     * @param set The new set of unique ids.
     */
    public void updateIDSet(Set<String> set) {
        this.idSet = set;
    }

}