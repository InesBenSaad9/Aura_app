package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.aura.dao.AppointmentDAO;
import tn.esprit.aura.entities.Appointment;
import tn.esprit.aura.utils.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AppointmentsView implements Initializable {

    @FXML private VBox appointmentsContainer;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int patientId = SessionManager.getCurrentUser().getId();
        List<Appointment> list = appointmentDAO.getAppointmentsByPatient(patientId);
        render(list);
    }

    private void render(List<Appointment> list) {
        appointmentsContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label e = new Label("Aucun rendez-vous.");
            e.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            appointmentsContainer.getChildren().add(e);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Appointment a : list) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 16; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

            Label icon = new Label("👨‍⚕️");
            icon.setStyle("-fx-font-size: 28px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label name = new Label("Dr. " + a.getDoctorName());
            name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            String dateStr = a.getDateTime() != null ? a.getDateTime().format(fmt) : "N/A";
            Label date = new Label("📅 " + dateStr);
            date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
            info.getChildren().addAll(name, date);

            String color = "Confirmé".equals(a.getStatus()) ? "#00b4ab" : "#f0a500";
            Label badge = new Label(a.getStatus());
            badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                    "-fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px;");

            row.getChildren().addAll(icon, info, badge);
            appointmentsContainer.getChildren().add(row);
        }
    }
}
