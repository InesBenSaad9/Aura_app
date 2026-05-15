package tn.esprit.aura.controllers.doctor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.aura.dao.MessageDAO;
import tn.esprit.aura.dao.AppointmentRequestDAO;
import tn.esprit.aura.entities.AppointmentRequest;
import tn.esprit.aura.utils.SessionManager;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class DoctorPatientsController implements Initializable {

    @FXML private VBox patientsContainer;
    private final MessageDAO messageDAO = new MessageDAO();
    private final AppointmentRequestDAO requestDAO = new AppointmentRequestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPatients();
    }

    private void loadPatients() {
        if (patientsContainer == null) return;
        patientsContainer.getChildren().clear();

        int myId = getRealDoctorId();
        Set<Integer> uniquePatientIds = new HashSet<>();

        // Patients from messages
        uniquePatientIds.addAll(messageDAO.getPatientPartnersForDoctor(myId));

        // Patients from appointments
        List<AppointmentRequest> reqs = requestDAO.getRequestsByDoctor(myId);
        for (AppointmentRequest req : reqs) {
            uniquePatientIds.add(req.getPatientId());
        }

        if (uniquePatientIds.isEmpty()) {
            Label empty = new Label("Aucun patient pour le moment.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            patientsContainer.getChildren().add(empty);
            return;
        }

        for (int patId : uniquePatientIds) {
            String name = messageDAO.resolveNameById(patId, "patient");
            patientsContainer.getChildren().add(createPatientCard(name, patId));
        }
    }

    private int getRealDoctorId() {
        String nom = SessionManager.getCurrentUser().getNom();
        if (nom == null || nom.trim().isEmpty()) return SessionManager.getCurrentUser().getId();
        try (java.sql.Connection conn = tn.esprit.aura.utils.DBConnection.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id FROM docteurs WHERE nom LIKE ?")) {
            ps.setString(1, "%" + nom.trim() + "%");
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            System.err.println("getRealDoctorId error: " + e.getMessage());
        }
        return SessionManager.getCurrentUser().getId();
    }

    private HBox createPatientCard(String name, int patId) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 8; -fx-border-color: #30363d; -fx-border-radius: 8; -fx-padding: 16 24;");

        Label icon = new Label("👤");
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button msgBtn = new Button("💬 Message");
        msgBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        msgBtn.setOnAction(e -> {
            tn.esprit.aura.controllers.shared.MessagesView.preSelectedContactId = patId;
            try {
                javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) msgBtn.getScene().getRoot();
                javafx.scene.control.Button msgSidebarBtn = (javafx.scene.control.Button) root.lookup("#userBtnMessages");
                if (msgSidebarBtn != null) {
                    msgSidebarBtn.fire();
                }
            } catch (Exception ex) {
                System.err.println("Error navigating to messages: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(icon, nameLbl, spacer, msgBtn);
        return card;
    }
}
