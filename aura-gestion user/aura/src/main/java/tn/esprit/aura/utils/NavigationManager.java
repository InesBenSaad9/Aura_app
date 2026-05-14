package tn.esprit.aura.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Centralised navigation helper — handles scene switching with fade transitions.
 */
public class NavigationManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navigate to a new FXML view with a fade-in transition.
     */
    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            root.setOpacity(0);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    NavigationManager.class.getResource("/tn/esprit/aura/styles/style.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.show();

            FadeTransition ft = new FadeTransition(Duration.millis(350), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            System.err.println("Navigation error → " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load an FXML fragment (for embedding in StackPane, etc.)
     */
    public static Parent loadFxml(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load FXML → " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
