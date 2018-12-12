package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a food item with all its properties.
 * @author d-team 57
 */
public class FoodItem {
    // The name of the food item.
    private String name;

    // The id of the food item.
    private String id;

    // Map of nutrients and value.
    private HashMap<String, Double> nutrients;
    
    /**
     * Constructor
     * @param name name of the food item
     * @param id unique id of the food item 
     */
    public FoodItem(String id, String name) {
        this.id = id;
        this.name = name;
    }
    /**
     * Constructs foodItem from a Food object, due to uncertainty about whether
     * FoodItem needs to be implemented if we didn't use it
     * @param Food
     */
     public FoodItem(Food food){
    	 this.id=food.getID();
    	 this.name=food.getName();
    	 for(String nutrient : Food.NUTRIENT_IDS){
    		 this.addNutrient(nutrient, food.getNutrientValue(nutrient));
    	 }
     }
    /**
     * Gets the name of the food item
     * 
     * @return name of the food item
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unique id of the food item
     * 
     * @return id of the food item
     */
    public String getID() {
        return this.id;
    }
    
    /**
     * Gets the nutrients of the food item
     * 
     * @return nutrients of the food item
     */
    public HashMap<String, Double> getNutrients() {
        return (HashMap<String, Double>) this.nutrients.clone();
    }

    /**
     * Adds a nutrient and its value to this food. 
     * If nutrient already exists, updates its value.
     */
    public void addNutrient(String name, double value) {
        this.nutrients.put(name, value);
    }

    /**
     * Returns the value of the given nutrient for this food item. 
     * If not present, then returns 0.
     */
    public double getNutrientValue(String name) {
        Double out = this.nutrients.get(name);
        return out==null?0:out;
    }
    
}