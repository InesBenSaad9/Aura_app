package org.example.ui.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.utils.SessionManager;

public class LoginView {

    @FXML private StackPane rootPane;

    @FXML
    private void goToPatient() {
        try {
            // Session patient par défaut (user Ines, id=1)
            SessionManager sm = SessionManager.getInstance();
            sm.setUserId(1);
            sm.setUsername("Ines");
            sm.setRole("patient");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/patient_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle("AURA — Espace Patient");
            stage.setScene(new Scene(root, 1200, 750));
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDoctor() {
        try {
            // Session docteur par défaut (Dr. Sonia Ben Salah Modifiée, id=1)
            SessionManager sm = SessionManager.getInstance();
            sm.setUserId(1);
            sm.setUsername("Dr. Sonia Ben Salah");
            sm.setRole("doctor");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/doctor_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle("AURA — Espace Docteur");
            stage.setScene(new Scene(root, 1200, 750));
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}