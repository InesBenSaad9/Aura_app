package tn.esprit.aura.controllers.patient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.aura.dao.PrescriptionDAO;
import tn.esprit.aura.entities.Prescription;
import tn.esprit.aura.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PatientMedicalView implements Initializable {

    @FXML private VBox prescriptionsContainer;

    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        prescriptionsContainer.getChildren().clear();
        int patientId = SessionManager.getCurrentUser().getId();
        List<Prescription> list = prescriptionDAO.getPrescriptionsByPatient(patientId);

        if (list.isEmpty()) {
            Label empty = new Label("Aucune prescription active.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            prescriptionsContainer.getChildren().add(empty);
            return;
        }
        for (Prescription p : list) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #0d1117; -fx-background-radius: 10; -fx-padding: 14; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");
            Label icon = new Label("💊");
            icon.setStyle("-fx-font-size: 22px;");
            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label med = new Label(p.getMedicament());
            med.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label dosage = new Label("Dosage : " + p.getDosage());
            dosage.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
            Label Doctor = new Label("Dr. " + p.getDoctorNom());
            Doctor.setStyle("-fx-text-fill: #00b4ab; -fx-font-size: 12px;");
            info.getChildren().addAll(med, dosage, Doctor);
            row.getChildren().addAll(icon, info);
            prescriptionsContainer.getChildren().add(row);
        }
    }
}
