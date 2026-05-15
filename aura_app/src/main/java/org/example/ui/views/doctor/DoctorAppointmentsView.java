package org.example.ui.views.doctor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.AppointmentDAO;
import org.example.entities.Appointment;
import org.example.utils.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorAppointmentsView implements Initializable {

    @FXML private VBox appointmentsContainer;
    @FXML private Label totalLabel, pendingLabel, confirmedLabel;

    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int docteurId = SessionManager.getInstance().getUserId();
        List<Appointment> list = appointmentDAO.getAppointmentsByDocteur(docteurId);

        long total     = list.size();
        long pending   = list.stream().filter(a -> "En attente".equals(a.getStatus())).count();
        long confirmed = list.stream().filter(a -> "Confirmé".equals(a.getStatus())).count();

        if (totalLabel     != null) totalLabel.setText(String.valueOf(total));
        if (pendingLabel   != null) pendingLabel.setText(String.valueOf(pending));
        if (confirmedLabel != null) confirmedLabel.setText(String.valueOf(confirmed));

        renderAppointments(list);
    }

    private void renderAppointments(List<Appointment> list) {
        appointmentsContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            appointmentsContainer.getChildren().add(empty);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        for (Appointment a : list) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 16; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

            Label icon = new Label("👤");
            icon.setStyle("-fx-font-size: 26px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label patient = new Label("Patient #" + a.getPatientId());
            patient.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            String dateStr = a.getDateTime() != null ? a.getDateTime().format(fmt) : "N/A";
            Label date = new Label("📅 " + dateStr);
            date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            info.getChildren().addAll(patient, date);

            String status = a.getStatus();
            String color = "Confirmé".equals(status) ? "#00b4ab" : "#f0a500";
            Label badge = new Label(status);
            badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                    "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;");

            row.getChildren().addAll(icon, info, badge);
            appointmentsContainer.getChildren().add(row);
        }
    }
}
