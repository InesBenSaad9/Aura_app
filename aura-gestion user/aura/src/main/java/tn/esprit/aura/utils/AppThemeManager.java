package tn.esprit.aura.utils;

import javafx.scene.Parent;
import javafx.scene.layout.Region;

public class AppThemeManager {
    private static Parent root;

    public static void setRoot(Parent rootPane) {
        root = rootPane;
    }

    public static void applyMoodTheme(String mood) {
        if (root == null) return;
        
        // Remove existing mood classes
        root.getStyleClass().removeAll("mood-calme", "mood-stresse", "mood-fatigue", "mood-energise", "mood-focalise");
        
        // Add new mood class
        String moodClass = "mood-" + mood.toLowerCase()
            .replace("é", "e")
            .replace("è", "e");
        root.getStyleClass().add(moodClass);
        
        System.out.println("Global theme applied: " + moodClass);
    }
}
