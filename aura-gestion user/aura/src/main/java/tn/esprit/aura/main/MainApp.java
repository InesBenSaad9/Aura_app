package tn.esprit.aura.main;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.aura.utils.DBInitializer;
import tn.esprit.aura.utils.NavigationManager;
import com.github.sarxos.webcam.Webcam;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialise database tables
        DBInitializer.initialize();
        
        // Start local web server for QR codes
        tn.esprit.aura.utils.LocalWebServerService.getInstance().startServer();

        // Initialize Google Calendar (in background to avoid blocking UI)
        new Thread(() -> tn.esprit.aura.utils.GoogleCalendarService.getInstance().init()).start();

        // Initialize Twilio
        tn.esprit.aura.utils.TwilioService.getInstance().init();

        // Set up the primary stage
        primaryStage.setTitle("AURA — Adaptive Urban Relationship Assistant");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        NavigationManager.setPrimaryStage(primaryStage);
        
        // Ensure all threads (like webcam and web server) close when app is closed
        primaryStage.setOnCloseRequest(event -> {
            try {
                tn.esprit.aura.utils.LocalWebServerService.getInstance().stopServer();
                for (Webcam w : Webcam.getWebcams()) {
                    if (w.isOpen()) w.close();
                }
            } catch (Exception e) {
                // Silently ignore errors during shutdown
            }
            System.exit(0);
        });

        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
