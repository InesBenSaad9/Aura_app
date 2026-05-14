package org.example.ui.views.patient;
import org.example.utils.NotificationService;
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
import java.util.stream.Collectors;

public class PatientAppointmentsView implements Initializable {

    @FXML private VBox appointmentsContainer;
    @FXML private Button btnAll, btnConfirmed, btnPending, btnCancelled;

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private List<Appointment> allAppointments;

    private static final String BTN_ACTIVE =
            "-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 20; -fx-padding: 6 18; -fx-cursor: hand; -fx-font-size: 13px;";
    private static final String BTN_INACTIVE =
            "-fx-background-color: #21262d; -fx-text-fill: #8b949e; -fx-font-size: 13px; " +
                    "-fx-background-radius: 20; -fx-padding: 6 18; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int patientId = SessionManager.getInstance().getUserId();
        allAppointments = appointmentDAO.getAppointmentsByPatient(patientId);
        renderAppointments(allAppointments);
    }

    @FXML private void filterAll() {
        setActive(btnAll);
        renderAppointments(allAppointments);
    }

    @FXML private void filterConfirmed() {
        setActive(btnConfirmed);
        renderAppointments(allAppointments.stream()
                .filter(a -> "Confirmé".equals(a.getStatus())).collect(Collectors.toList()));
    }

    @FXML private void filterPending() {
        setActive(btnPending);
        renderAppointments(allAppointments.stream()
                .filter(a -> "En attente".equals(a.getStatus())).collect(Collectors.toList()));
    }

    @FXML private void filterCancelled() {
        setActive(btnCancelled);
        renderAppointments(allAppointments.stream()
                .filter(a -> "Annulé".equals(a.getStatus())).collect(Collectors.toList()));
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnAll, btnConfirmed, btnPending, btnCancelled}) {
            b.setStyle(BTN_INACTIVE);
        }
        active.setStyle(BTN_ACTIVE);
    }

    private void renderAppointments(List<Appointment> list) {
        appointmentsContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous trouvé.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            appointmentsContainer.getChildren().add(empty);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        for (Appointment appt : list) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 18; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

            Label avatar = new Label("👨‍⚕️");
            avatar.setStyle("-fx-font-size: 30px; -fx-background-color: linear-gradient(#6e40c9, #4a9eff); " +
                    "-fx-background-radius: 30; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");

            VBox info = new VBox(5);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label name = new Label("Dr. " + appt.getDocteurName());
            name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
            Label spec = new Label(appt.getSpecialite());
            spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            String dateStr = appt.getDateTime() != null ? appt.getDateTime().format(fmt) : "Date inconnue";
            Label date = new Label("📅 " + dateStr);
            date.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
            info.getChildren().addAll(name, spec, date);

            String status = appt.getStatus();
            String color = switch (status) {
                case "Confirmé" -> "#00b4ab";
                case "Annulé"   -> "#f85149";
                default         -> "#f0a500";
            };
            Label badge = new Label(status);
            badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                    "-fx-background-radius: 16; -fx-padding: 4 14; -fx-font-size: 12px; -fx-font-weight: bold;");

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER);
            actions.getChildren().add(badge);
            if (!"Annulé".equals(status)) {
                Button cancelBtn = new Button("Annuler");
                cancelBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; " +
                        "-fx-background-radius: 16; -fx-padding: 4 14; -fx-cursor: hand; -fx-font-size: 12px;");
                int id = appt.getId();
                cancelBtn.setOnAction(e -> {
                    appointmentDAO.cancelAppointment(id);
                    int patientId = SessionManager.getInstance().getUserId();
                    allAppointments = appointmentDAO.getAppointmentsByPatient(patientId);
                    filterAll();
                });
                actions.getChildren().add(cancelBtn);
            }
            row.getChildren().addAll(avatar, info, actions);
            appointmentsContainer.getChildren().add(row);
        }
    }
}
