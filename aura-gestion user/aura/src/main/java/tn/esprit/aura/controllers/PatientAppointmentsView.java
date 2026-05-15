package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.aura.dao.AppointmentDAO;
import tn.esprit.aura.entities.Appointment;
import tn.esprit.aura.utils.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PatientAppointmentsView implements Initializable {

    @FXML private VBox appointmentsContainer;
    @FXML private Button btnAll, btnConfirmed, btnPending, btnCancelled;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private List<Appointment> allAppointments;

    private static final String BTN_ACTIVE =
            "-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 20; -fx-padding: 6 18; -fx-cursor: hand; -fx-font-size: 13px;";
    private static final String BTN_INACTIVE =
            "-fx-background-color: #21262d; -fx-text-fill: #8b949e; -fx-font-size: 13px; " +
                    "-fx-background-radius: 20; -fx-padding: 6 18; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isLoggedIn()) return;
        int patientId = SessionManager.getCurrentUser().getId();
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
            if (b != null) b.setStyle(BTN_INACTIVE);
        }
        if (active != null) active.setStyle(BTN_ACTIVE);
    }

    private void renderAppointments(List<Appointment> list) {
        if (appointmentsContainer == null) return;
        appointmentsContainer.getChildren().clear();
        
        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous trouvé.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            appointmentsContainer.getChildren().add(empty);
            return;
        }
        
        for (Appointment appointment : list) {
            appointmentsContainer.getChildren().add(createAppointmentRow(appointment));
        }
    }

    private HBox createAppointmentRow(Appointment appointment) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 18; " +
                "-fx-border-color: #21262d; -fx-border-radius: 12; -fx-border-width: 1;");

        Label avatar = new Label("👨‍⚕️");
        avatar.setStyle("-fx-font-size: 30px; -fx-background-color: #21262d; " +
                "-fx-background-radius: 30; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label("Dr. " + appointment.getDocteurName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label spec = new Label(appointment.getSpecialite());
        spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        String dateStr = appointment.getDateTime() != null ? appointment.getDateTime().format(fmt) : "Date inconnue";
        Label date = new Label("📅 " + dateStr);
        date.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
        info.getChildren().addAll(name, spec, date);

        String status = appointment.getStatus();
        String color = switch (status != null ? status : "") {
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
            cancelBtn.setOnAction(e -> {
                appointmentDAO.cancelAppointment(appointment.getId());
                int patientId = SessionManager.getCurrentUser().getId();
                allAppointments = appointmentDAO.getAppointmentsByPatient(patientId);
                filterAll();
            });
            actions.getChildren().add(cancelBtn);
        }
        
        row.getChildren().addAll(avatar, info, actions);
        return row;
    }
}
