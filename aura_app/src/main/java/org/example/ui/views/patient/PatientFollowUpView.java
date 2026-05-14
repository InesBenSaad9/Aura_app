package org.example.ui.views.patient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.TherapySessionDAO;
import org.example.entities.TherapySession;
import org.example.utils.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientFollowUpView implements Initializable {

    @FXML private VBox sessionsContainer;
    @FXML private Label sessionCountLabel;

    private TherapySessionDAO sessionDAO = new TherapySessionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSessions();
    }

    private void loadSessions() {
        sessionsContainer.getChildren().clear();
        int patientId = SessionManager.getInstance().getUserId();
        List<TherapySession> sessions = sessionDAO.getSessionsByPatient(patientId);

        if (sessionCountLabel != null) {
            sessionCountLabel.setText(sessions.size() + " séances");
        }

        if (sessions.isEmpty()) {
            Label empty = new Label("Aucune séance enregistrée.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            sessionsContainer.getChildren().add(empty);
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (TherapySession s : sessions) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 16; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

            Label icon = new Label("🗓️");
            icon.setStyle("-fx-font-size: 26px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            String dateStr = s.getSessionDate() != null ? s.getSessionDate().format(fmt) : "Date inconnue";
            Label date = new Label(dateStr);
            date.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            if (s.getNotes() != null && !s.getNotes().isEmpty()) {
                Label notes = new Label(s.getNotes());
                notes.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
                notes.setWrapText(true);
                info.getChildren().addAll(date, notes);
            } else {
                info.getChildren().add(date);
            }

            String status = s.getStatus() != null ? s.getStatus() : "SCHEDULED";
            String color = "COMPLETED".equals(status) ? "#00b4ab" : "#f0a500";
            Label badge = new Label(status);
            badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                    "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

            row.getChildren().addAll(icon, info, badge);
            sessionsContainer.getChildren().add(row);
        }
    }
}
