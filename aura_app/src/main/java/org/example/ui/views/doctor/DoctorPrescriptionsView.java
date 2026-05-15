package org.example.ui.views.doctor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.DatabaseConnection;
import org.example.dao.PrescriptionDAO;
import org.example.entities.Prescription;
import org.example.utils.NotificationService;
import org.example.utils.SessionManager;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorPrescriptionsView implements Initializable {

    @FXML private ComboBox<String> patientCombo;
    @FXML private TextField medicamentField;
    @FXML private TextField dosageField;
    @FXML private TextArea instructionsField;
    @FXML private Label statusLabel;
    @FXML private VBox prescriptionsContainer;
    @FXML private Label formTitle;
    @FXML private Button saveBtn;
    @FXML private Button cancelEditBtn;

    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private int docteurId;
    private int editingPrescriptionId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        docteurId = SessionManager.getInstance().getUserId();
        loadPatientCombo();
        loadPrescriptions();
    }

    private void loadPatientCombo() {
        patientCombo.getItems().clear();
        List<String> names = prescriptionDAO.getPatientNamesForDocteur(docteurId);
        patientCombo.getItems().addAll(names);
    }

    @FXML
    private void savePrescription() {
        String patUsername = patientCombo.getValue();
        String med         = medicamentField.getText().trim();
        String dosage      = dosageField.getText().trim();
        String instr       = instructionsField != null ? instructionsField.getText().trim() : "";

        if ((editingPrescriptionId == -1 && patUsername == null) || med.isEmpty() || dosage.isEmpty()) {
            setStatus("⚠ Veuillez remplir tous les champs obligatoires.", "#f85149");
            return;
        }

        // ── Mode édition ──────────────────────────────────────────────────────
        if (editingPrescriptionId != -1) {
            boolean ok = prescriptionDAO.updatePrescription(editingPrescriptionId, docteurId, med, dosage, instr);
            if (ok) {
                setStatus("Prescription modifiée avec succès.", "#00b4ab");
                clearForm();
                loadPrescriptions();
            } else {
                setStatus("Erreur lors de la modification.", "#f85149");
            }
            return;
        }

        // ── Nouvelle prescription ─────────────────────────────────────────────
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            ps.setString(1, patUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int patId = rs.getInt("id");
                boolean ok = prescriptionDAO.createPrescription(patId, docteurId, med, dosage, instr);
                if (ok) {
                    // ✅ NOTIFICATION prescription créée
                    NotificationService.prescription(med, patUsername);
                    setStatus("✓ Prescription sauvegardée avec succès !", "#00b4ab");
                    clearForm();
                    loadPrescriptions();
                } else {
                    setStatus("Erreur lors de la sauvegarde.", "#f85149");
                    NotificationService.error("Impossible de sauvegarder la prescription.");
                }
            } else {
                setStatus("Patient introuvable.", "#f85149");
            }
        } catch (Exception e) {
            setStatus("Erreur : " + e.getMessage(), "#f85149");
            NotificationService.error("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit() {
        clearForm();
        setStatus("", "#8b949e");
    }

    private void loadPrescriptions() {
        prescriptionsContainer.getChildren().clear();
        List<Prescription> list = prescriptionDAO.getPrescriptionsByDocteur(docteurId);

        if (list.isEmpty()) {
            Label empty = new Label("Aucune prescription émise.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            prescriptionsContainer.getChildren().add(empty);
            return;
        }

        for (Prescription p : list) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 14; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");

            Label icon = new Label("💊");
            icon.setStyle("-fx-font-size: 24px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label patient = new Label("👤 " + p.getPatientUsername());
            patient.setStyle("-fx-text-fill: #00b4ab; -fx-font-weight: bold; -fx-font-size: 13px;");
            Label med = new Label(p.getMedicament());
            med.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            Label dosageLbl = new Label("Dosage : " + p.getDosage());
            dosageLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
            info.getChildren().addAll(patient, med, dosageLbl);
            if (p.getInstructions() != null && !p.getInstructions().isBlank()) {
                Label instructions = new Label("Instructions : " + p.getInstructions());
                instructions.setWrapText(true);
                instructions.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
                info.getChildren().add(instructions);
            }

            Label date = new Label(p.getDateCreation() != null ? p.getDateCreation().toString() : "");
            date.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 11px; " +
                    "-fx-background-color: #21262d; -fx-background-radius: 8; -fx-padding: 3 10;");

            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-padding: 7 12; -fx-cursor: hand;");
            editBtn.setOnAction(e -> startEdit(p));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: #da3633; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-padding: 7 12; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deletePrescription(p));

            HBox actions = new HBox(8, editBtn, deleteBtn);
            actions.setAlignment(Pos.CENTER_RIGHT);
            actions.setPadding(new Insets(0, 0, 0, 8));

            row.getChildren().addAll(icon, info, date, actions);
            prescriptionsContainer.getChildren().add(row);
        }
    }

    private void startEdit(Prescription prescription) {
        editingPrescriptionId = prescription.getId();
        patientCombo.setValue(prescription.getPatientUsername());
        patientCombo.setDisable(true);
        medicamentField.setText(prescription.getMedicament());
        dosageField.setText(prescription.getDosage());
        if (instructionsField != null) {
            instructionsField.setText(prescription.getInstructions() == null ? "" : prescription.getInstructions());
        }
        if (formTitle != null) formTitle.setText("Modifier la prescription");
        if (saveBtn != null) saveBtn.setText("Enregistrer les modifications");
        if (cancelEditBtn != null) {
            cancelEditBtn.setVisible(true);
            cancelEditBtn.setManaged(true);
        }
        setStatus("Modification de la prescription sélectionnée.", "#8b949e");
    }

    private void deletePrescription(Prescription prescription) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la prescription");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la prescription de " + prescription.getPatientUsername() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = prescriptionDAO.deletePrescription(prescription.getId(), docteurId);
            if (ok) {
                if (editingPrescriptionId == prescription.getId()) clearForm();
                setStatus("Prescription supprimée.", "#00b4ab");
                loadPrescriptions();
            } else {
                setStatus("Erreur lors de la suppression.", "#f85149");
            }
        }
    }

    private void clearForm() {
        editingPrescriptionId = -1;
        medicamentField.clear();
        dosageField.clear();
        if (instructionsField != null) instructionsField.clear();
        patientCombo.setValue(null);
        patientCombo.setDisable(false);
        if (formTitle != null) formTitle.setText("Nouvelle prescription");
        if (saveBtn != null) saveBtn.setText("Sauvegarder");
        if (cancelEditBtn != null) {
            cancelEditBtn.setVisible(false);
            cancelEditBtn.setManaged(false);
        }
    }

    private void setStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
    }
}