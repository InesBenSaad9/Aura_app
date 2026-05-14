package org.example.ui.views.patient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.PrescriptionDAO;
import org.example.entities.Prescription;
import org.example.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PatientPrescriptionsView implements Initializable {

    @FXML private VBox prescriptionsContainer;
    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        prescriptionsContainer.getChildren().clear();
        int patientId = SessionManager.getInstance().getUserId();
        List<Prescription> list = prescriptionDAO.getPrescriptionsByPatient(patientId);

        if (list.isEmpty()) {
            Label empty = new Label("Aucune prescription pour le moment.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            prescriptionsContainer.getChildren().add(empty);
            return;
        }

        for (Prescription p : list) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 18; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

            Label icon = new Label("💊");
            icon.setStyle("-fx-font-size: 30px;");

            VBox info = new VBox(5);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label med = new Label(p.getMedicament());
            med.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
            Label dosage = new Label("Dosage : " + p.getDosage());
            dosage.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            Label doc = new Label("Prescrit par Dr. " + p.getDocteurUsername());
            doc.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
            if (p.getInstructions() != null && !p.getInstructions().isEmpty()) {
                Label instr = new Label("ℹ " + p.getInstructions());
                instr.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
                instr.setWrapText(true);
                info.getChildren().addAll(med, dosage, doc, instr);
            } else {
                info.getChildren().addAll(med, dosage, doc);
            }

            // Date badge
            Label dateLbl = new Label(p.getDateCreation() != null ? p.getDateCreation().toString() : "");
            dateLbl.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 11px; " +
                    "-fx-background-color: #21262d; -fx-background-radius: 8; -fx-padding: 3 10;");

            row.getChildren().addAll(icon, info, dateLbl);
            prescriptionsContainer.getChildren().add(row);
        }
    }
}
