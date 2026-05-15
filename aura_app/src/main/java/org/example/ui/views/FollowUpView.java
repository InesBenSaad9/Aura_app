package org.example.ui.views;

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

public class FollowUpView implements Initializable {

    @FXML private VBox sessionsContainer;
    private TherapySessionDAO sessionDAO = new TherapySessionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int patientId = SessionManager.getInstance().getUserId();
        List<TherapySession> sessions = sessionDAO.getSessionsByPatient(patientId);
        render(sessions);
    }

    private void render(List<TherapySession> sessions) {
        sessionsContainer.getChildren().clear();
        if (sessions.isEmpty()) {
            Label e = new Label("Aucune séance enregistrée.");
            e.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            sessionsContainer.getChildren().add(e);
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
            String dateStr = s.getSessionDate() != null ? s.getSessionDate().format(fmt) : "N/A";
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

            row.getChildren().addAll(icon, info);
            sessionsContainer.getChildren().add(row);
        }
    }
}
