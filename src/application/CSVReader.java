package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CSVReader {
   
    /*
     * Reads info from CSV file and returns an ArrayList of 
     * the Food items it contains.
     * 
     * @param filePath String containing path to CSV file
     * @return ArrayList of Food items from file
     */
    public ArrayList<Food> read(String filePath) {
        ArrayList<Food> foodList = new ArrayList<Food>(); // ArrayList of Foods from the file
        BufferedReader br = null; 
        String line = "";
        String csvSplitBy = ","; // split lines based on commas
        
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] foodData = line.split(csvSplitBy);
                
                // line does not have the proper amount of data points, so skip
                if (foodData.length != 12) {
                    continue;
                }
               
                // convert input to proper format, create Food and add to list
                String id = "invalidID";
                String name = "invalidName";
                double calories = -1;
                double fat = -1;
                double carbs = -1;
                double fiber = -1;
                double protein = -1;
                
                try {
                    id = foodData[0];
                    name = foodData[1];
                    calories = Double.parseDouble(foodData[3]);
                    fat = Double.parseDouble(foodData[5]);
                    carbs = Double.parseDouble(foodData[7]);
                    fiber = Double.parseDouble(foodData[9]);
                    protein = Double.parseDouble(foodData[11]);
                } catch (NumberFormatException e) {
                   continue;
                }
                
                foodList.add(new Food(id, name, calories, fat, carbs, fiber, protein));               
            }          
        } catch (FileNotFoundException e) {
            // tell user that file could not be found
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("File Not Found");
            alert.setHeaderText(null);
            alert.setContentText("The file " + filePath + " could not be found. Could not load data.");
            alert.showAndWait();
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {                   
                }
            }
        }        
        return foodList;
    }
        
 }
