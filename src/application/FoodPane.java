package application;

import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

public class FoodPane extends BorderPane{
    private TableView foodTable = new TableView(); // table to display food options
    private MealListPane mlp; // reference to meal list pane
    TextField filterField;
    
    /*
     * Constructs a FoodPane containing information
     * about the name and nutritional content of various foods.
     * @param mlp A reference to the MealListPane that will be fed data from the FoodPane.
     */
    public FoodPane(MealListPane mlp) {
        try {
            this.mlp = mlp;
            this.setRight(foodPane());
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
        Label foodLabel = new Label("Food List:");
        foodLabel.setId("section-heading");
        this.filterField = new TextField();
        this.filterField.setPromptText("filter name here");
        Button addFoodBtn = new Button("Add food(s) to meal");
        
        // name the columns
        setupColumns();
        
        // format table size and enable selection of multiple foods at once
        this.foodTable.setPrefWidth(500);
        this.foodTable.setPrefHeight(500);
        this.foodTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.foodTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Set up and display scene
        VBox foodPane = new VBox(10);
        foodPane.setId("food-data");
        foodPane.setPadding(new Insets(20, 20, 20, 20));
        foodPane.getChildren().addAll(foodLabel, filterField, foodTable, addFoodBtn);
        
        // when button is pressed, add selected food item(s) to meal list
        addFoodBtn.setOnAction(new EventHandler<ActionEvent>() {
            
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
        return foodPane;
    }
    
    protected void updateMealListPane(ArrayList<Food> selectedArr) {
        this.mlp.updateMlpData(selectedArr);       
    }

    /*
     * Takes in an ArrayList of FoodItems and fills the Table
     * with those foods.
     * @param food The ArrayList of FoodItems
     */
     //TODO: maybe make foodObsList a class variable? I think to add new elements, you'll need to access it
     //Or maybe filteredData?

    public void updateFoodPaneData(ArrayList<Food> food) {
        ObservableList<Food> foodObsList = FXCollections.observableList(food);
        //taken from https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        FilteredList<Food> filteredData = new FilteredList<Food>(foodObsList, p->true);

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
        SortedList<Food> sortedData = new SortedList<Food>(filteredData);
        //tbh, not sure what's the diff between Observable, Filtered, and Sorted list.
        sortedData.comparatorProperty().bind(this.foodTable.comparatorProperty());
        this.foodTable.setItems(sortedData);        
      
    }
    
    
    /*
     * Sets up the columns in the table.
     * Sets up Name, Calories, Fat, Carbs, Fiber, and Protein
     * columns in that order.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupColumns() {
        // name
        TableColumn nameCol = new TableColumn("Name");
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
        
        // set the height of Food Table to a ratio of the screen's height
        this.foodTable.setMinHeight((0.60) * Screen.getPrimary().getBounds().getHeight());
    }

}
