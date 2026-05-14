package org.example.mainn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.GoogleCalendarService;
import org.example.utils.NotificationService;
import org.example.utils.SessionManager;
import org.example.utils.TwilioService;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        TwilioService.getInstance().init();
        TwilioService.getInstance().sendSMS("+21695670354", "Test AURA !");
        GoogleCalendarService.getInstance().init();
        primaryStage = stage;
        primaryStage.setTitle("AURA - AI Life Companion");

        // ✅ AJOUT : Initialiser NotificationService avec le stage
        NotificationService.getInstance().setStage(stage);

        showLogin();
        primaryStage.show();
    }

    public static void showLogin() {
        loadFXML("/org/example/login.fxml", 900, 650, false);
    }

    public static void showPatientDashboard() {
        loadFXML("/org/example/patient_dashboard.fxml", 1200, 750, true);
    }

    public static void showDoctorDashboard() {
        loadFXML("/org/example/doctor_dashboard.fxml", 1200, 750, true);
    }

    private static void loadFXML(String path, double w, double h, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(path));
            Parent root  = loader.load();
            Scene  scene = new Scene(root, w, h);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(maximized);

            // ✅ AJOUT : Mettre à jour le stage dans NotificationService après chaque changement de scène
            NotificationService.getInstance().setStage(primaryStage);

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger " + path);
            e.printStackTrace();
        }
    }

    public static void redirectAfterLogin() {
        String role = SessionManager.getInstance().getRole();
        if ("DOCTOR".equals(role)) {
            showDoctorDashboard();
        } else {
            showPatientDashboard();
        }
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}