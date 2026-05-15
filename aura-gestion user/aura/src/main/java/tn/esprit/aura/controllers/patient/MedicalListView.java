package tn.esprit.aura.controllers.patient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.aura.dao.AppointmentDAO;
import tn.esprit.aura.dao.AppointmentRequestDAO;
import tn.esprit.aura.dao.PrescriptionDAO;
import tn.esprit.aura.entities.Appointment;
import tn.esprit.aura.entities.AppointmentRequest;
import tn.esprit.aura.entities.Prescription;
import tn.esprit.aura.entities.Doctor;
import tn.esprit.aura.dao.DoctorDAO;
import tn.esprit.aura.utils.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MedicalListView implements Initializable {

    @FXML private VBox appointmentsContainer;
    @FXML private VBox prescriptionsContainer;
    @FXML private FlowPane doctorsContainer;
    @FXML private TextField searchField;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final AppointmentRequestDAO requestDAO = new AppointmentRequestDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDoctors();
        loadAppointments();
        loadPrescriptions();
    }

    private void loadDoctors() {
        if (doctorsContainer == null) return;
        doctorsContainer.getChildren().clear();

        // Ensure Dr Eya Rajai exists
        List<Doctor> allDoctors = doctorDAO.getAllDoctors();
        boolean eyaExists = allDoctors.stream().anyMatch(d -> d.getNom() != null && d.getNom().toLowerCase().contains("eya rajai"));
        
        if (!eyaExists) {
            Doctor eya = new Doctor(0, "Dr. Eya Rajai", "eyarajai", "password", "Psychologue", "Tunis", "71555666", 5.0);
            doctorDAO.addDoctor(eya);
            allDoctors = doctorDAO.getAllDoctors(); // Reload after insert
        }

        for (Doctor doc : allDoctors) {
            doctorsContainer.getChildren().add(createDoctorCard(doc));
        }
    }

    @FXML
    private void handleSearch() {
        if (searchField == null || doctorsContainer == null) return;
        String query = searchField.getText().toLowerCase().trim();
        doctorsContainer.getChildren().clear();
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        for (Doctor doc : doctors) {
            if (query.isEmpty() || (doc.getNom() != null && doc.getNom().toLowerCase().contains(query)) ||
                (doc.getSpecialite() != null && doc.getSpecialite().toLowerCase().contains(query))) {
                doctorsContainer.getChildren().add(createDoctorCard(doc));
            }
        }
    }

    private VBox createDoctorCard(Doctor doc) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 20; " +
                "-fx-border-color: #30363d; -fx-border-radius: 12; -fx-border-width: 1; -fx-min-width: 220;");

        // Avatar
        StackPane avatarPane = new StackPane();
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(35, javafx.scene.paint.Color.web("#6B5FD4"));
        Label icon = new Label("👨‍⚕️");
        icon.setStyle("-fx-font-size: 30px; -fx-text-fill: white;");
        avatarPane.getChildren().addAll(circle, icon);

        // Name
        Label nameLbl = new Label(doc.getNom() != null ? doc.getNom() : doc.getUsername());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        nameLbl.setWrapText(true);
        nameLbl.setAlignment(Pos.CENTER);

        // Specialty (use specialite or default)
        String specStr = (doc.getSpecialite() != null && !doc.getSpecialite().isEmpty()) ? doc.getSpecialite() : "Médecin Généraliste";
        Label specLbl = new Label(specStr);
        specLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");

        // Rating
        Label ratingLbl = new Label("★ " + doc.getRating());
        ratingLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        // Location
        String locStr = (doc.getVille() != null && !doc.getVille().isEmpty()) ? doc.getVille() : "Tunis";
        Label locLbl = new Label("📍 " + locStr);
        locLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        // Phone
        String telStr = (doc.getTelephone() != null && !doc.getTelephone().isEmpty()) ? doc.getTelephone() : "71000111";
        Label telLbl = new Label("📞 " + telStr);
        telLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        // Button
        Button btn = new Button("Prendre RDV");
        btn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand; -fx-min-width: 160;");
        btn.setOnAction(e -> {
            showRdvDialog(doc);
        });
        
        VBox.setMargin(btn, new javafx.geometry.Insets(10, 0, 0, 0));

        card.getChildren().addAll(avatarPane, nameLbl, specLbl, ratingLbl, locLbl, telLbl, btn);
        return card;
    }

    private void showRdvDialog(Doctor doc) {
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Prendre un RDV");
        dialog.setHeaderText(null);
        
        // Match the dark theme
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1c2026;");
        
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20; -fx-background-color: #1c2026;");
        
        // Header Label
        Label title = new Label("Demander un RDV avec " + (doc.getNom() != null ? doc.getNom() : doc.getUsername()));
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        
        // Subtitle
        String spec = (doc.getSpecialite() != null && !doc.getSpecialite().isEmpty()) ? doc.getSpecialite() : "Médecin Généraliste";
        String ville = (doc.getVille() != null && !doc.getVille().isEmpty()) ? doc.getVille() : "Tunis";
        Label subtitle = new Label(spec + " — " + ville);
        subtitle.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
        
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 400, 0);
        line.setStroke(javafx.scene.paint.Color.web("#30363d"));
        
        // Date
        Label dateLbl = new Label("Date souhaitée :");
        dateLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Choisir une date");
        datePicker.setStyle("-fx-control-inner-background: #0d1117; -fx-text-fill: black; -fx-pref-width: 400;");
        
        // Time
        Label timeLbl = new Label("Heure souhaitée (HH:mm) :");
        timeLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
        TextField timeField = new TextField("09:00");
        timeField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10; -fx-pref-width: 400;");
        
        // Motif
        Label motifLbl = new Label("Motif (optionnel) :");
        motifLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
        TextField motifField = new TextField();
        motifField.setPromptText("Ex: Consultation, Suivi, Anxiété...");
        motifField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10; -fx-pref-width: 400;");
        
        // Info
        Label infoLbl = new Label("i Le docteur recevra votre demande et pourra l'accepter ou la refuser");
        infoLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        
        content.getChildren().addAll(title, subtitle, line, dateLbl, datePicker, timeLbl, timeField, motifLbl, motifField, infoLbl);
        dialogPane.setContent(content);
        
        // Buttons
        ButtonType sendType = new ButtonType("Envoyer la demande", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(sendType, cancelType);
        
        // Style Buttons
        javafx.scene.Node sendBtn = dialogPane.lookupButton(sendType);
        sendBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        
        javafx.scene.Node cancelBtn = dialogPane.lookupButton(cancelType);
        cancelBtn.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8;");
        
        dialog.setResultConverter(b -> {
            if (b == sendType) {
                if (datePicker.getValue() == null) {
                    // Just fallback to now if empty
                    return sendType;
                }
                java.time.LocalDate date = datePicker.getValue();
                String t = timeField.getText().trim();
                if (!t.matches("\\d{2}:\\d{2}")) t = "09:00";
                java.time.LocalTime time = java.time.LocalTime.parse(t);
                java.time.LocalDateTime dt = java.time.LocalDateTime.of(date, time);
                
                if (SessionManager.isLoggedIn()) {
                    requestDAO.createRequest(SessionManager.getCurrentUser().getId(), doc.getId(), dt);
                    loadAppointments(); // refresh
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }


    private void loadAppointments() {
        if (appointmentsContainer == null) return;
        appointmentsContainer.getChildren().clear();
        
        if (!SessionManager.isLoggedIn()) return;
        int patientId = SessionManager.getCurrentUser().getId();

        // 1. Rendez-vous confirmés
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
        for (Appointment appointment : appointments) {
            appointmentsContainer.getChildren().add(createAppointmentRow(appointment));
        }

        // 2. Demandes en attente
        List<AppointmentRequest> requests = requestDAO.getRequestsByPatient(patientId);
        for (AppointmentRequest request : requests) {
            if ("PENDING".equalsIgnoreCase(request.getStatus())) {
                appointmentsContainer.getChildren().add(createPendingRequestRow(request));
            }
        }
    }

    private HBox createAppointmentRow(Appointment appointment) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 16; " +
                "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");

        Label av = new Label("👨‍⚕️");
        av.setStyle("-fx-font-size: 28px; -fx-background-color: #21262d; " +
                "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label doctorName = new Label(appointment.getDocteurName());
        doctorName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label spec = new Label(appointment.getSpecialite());
        spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        String dateStr = appointment.getDateTime() != null ? appointment.getDateTime().format(fmt) : "N/A";
        Label date = new Label("📅 " + dateStr);
        date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        info.getChildren().addAll(doctorName, spec, date);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        String status = appointment.getStatus();
        String statusColor = "Confirmé".equals(status) ? "#00b4ab" : "#f0a500";
        Label statusLbl = new Label(status);
        statusLbl.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; -fx-background-radius: 16; " +
                "-fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 12px;");
        cancelBtn.setOnAction(e -> {
            appointmentDAO.cancelAppointment(appointment.getId());
            loadAppointments();
        });

        actions.getChildren().addAll(statusLbl, cancelBtn);
        row.getChildren().addAll(av, info, actions);
        return row;
    }

    private HBox createPendingRequestRow(AppointmentRequest request) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 16; " +
                "-fx-border-color: #f0a500; -fx-border-radius: 10; -fx-border-width: 1;");

        Label av = new Label("RDV");
        av.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #f0a500; " +
                "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label doctorName = new Label(request.getDocteurName());
        doctorName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label spec = new Label(request.getSpecialite());
        spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        
        String dateStr = request.getAppointmentDate() != null ? request.getAppointmentDate().format(fmt) : "Date non définie";
        Label date = new Label(dateStr);
        date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        info.getChildren().addAll(doctorName, spec, date);

        Label badge = new Label("En attente");
        badge.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; " +
                "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button deleteBtn = new Button("Annuler");
        deleteBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; -fx-background-radius: 16; " +
                "-fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> {
            int patientId = SessionManager.getCurrentUser().getId();
            requestDAO.deletePendingRequest(request.getId(), patientId);
            loadAppointments();
        });

        HBox actions = new HBox(8, badge, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        row.getChildren().addAll(av, info, actions);
        return row;
    }

    private void loadPrescriptions() {
        if (prescriptionsContainer == null) return;
        prescriptionsContainer.getChildren().clear();
        
        if (!SessionManager.isLoggedIn()) return;
        int patientId = SessionManager.getCurrentUser().getId();
        List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByPatient(patientId);

        if (prescriptions.isEmpty()) {
            Label empty = new Label("Aucune prescription active.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            prescriptionsContainer.getChildren().add(empty);
            return;
        }

        for (Prescription p : prescriptions) {
            prescriptionsContainer.getChildren().add(createPrescriptionRow(p));
        }
    }

    private HBox createPrescriptionRow(Prescription p) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 14; " +
                "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");

        Label icon = new Label("💊");
        icon.setStyle("-fx-font-size: 24px;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label med = new Label(p.getMedicament());
        med.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label dosage = new Label("Dosage: " + p.getDosage());
        dosage.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        Label doc = new Label("Prescrit par Dr. " + p.getDocteurUsername());
        doc.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
        info.getChildren().addAll(med, dosage, doc);

        row.getChildren().addAll(icon, info);
        return row;
    }
}
