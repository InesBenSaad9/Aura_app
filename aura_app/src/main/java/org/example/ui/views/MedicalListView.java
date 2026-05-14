package org.example.ui.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.*;
import org.example.entities.*;
import org.example.utils.SessionManager;
import org.example.utils.NotificationService;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class MedicalListView implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane doctorsContainer;
    @FXML private VBox appointmentsContainer;
    @FXML private VBox prescriptionsContainer;

    private ServiceDocteur serviceDocteur = new ServiceDocteur();
    private AppointmentRequestDAO requestDAO = new AppointmentRequestDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDoctors(serviceDocteur.getAllDocteurs());
        loadAppointments();
        loadPrescriptions();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        List<docteur> results = keyword.isEmpty()
                ? serviceDocteur.getAllDocteurs()
                : serviceDocteur.searchDocteurs(keyword);
        loadDoctors(results);
    }

    private void loadDoctors(List<docteur> doctors) {
        doctorsContainer.getChildren().clear();
        for (docteur doc : doctors) {
            doctorsContainer.getChildren().add(createDoctorCard(doc));
        }
        if (doctors.isEmpty()) {
            Label noResult = new Label("Aucun médecin trouvé.");
            noResult.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            doctorsContainer.getChildren().add(noResult);
        }
    }

    private VBox createDoctorCard(docteur doc) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 24 16; " +
                "-fx-border-color: #30363d; -fx-border-radius: 12; -fx-border-width: 1;");

        // Avatar
        Label avatar = new Label("👨‍⚕️");
        avatar.setStyle("-fx-font-size: 40px; -fx-background-color: linear-gradient(#6e40c9, #4a9eff); " +
                "-fx-background-radius: 40; -fx-min-width: 80; -fx-min-height: 80; -fx-alignment: center;");

        // Name
        Label name = new Label(doc.getNom() != null ? doc.getNom() : "Dr. " + doc.getUsername());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        // Specialite
        Label spec = new Label(doc.getSpecialite());
        spec.setStyle("-fx-font-size: 13px; -fx-text-fill: #8b949e;");

        // Rating
        HBox ratingBox = new HBox(6);
        ratingBox.setAlignment(Pos.CENTER);
        Label star = new Label("⭐");
        Label rating = new Label(String.valueOf(doc.getRating()));
        rating.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        ratingBox.getChildren().addAll(star, rating);

        // Location
        Label ville = new Label("📍 " + doc.getVille());
        ville.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        // Phone
        Label phone = new Label("📞 " + (doc.getTelephone() != null ? doc.getTelephone() : "N/A"));
        phone.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        // Button
        Button rdvBtn = new Button("Prendre RDV");
        rdvBtn.setMaxWidth(Double.MAX_VALUE);
        rdvBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; -fx-font-size: 13px;");
        rdvBtn.setOnAction(e -> showAppointmentPopup(doc, null));

        card.getChildren().addAll(avatar, name, spec, ratingBox, ville, phone, rdvBtn);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: #30363d;", "-fx-border-color: #00b4ab;")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: #00b4ab;", "-fx-border-color: #30363d;")));

        return card;
    }

    private void showAppointmentPopup(docteur doc, AppointmentRequest existingRequest) {
        boolean editMode = existingRequest != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Modifier le RDV" : "Prendre un RDV");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #161b22;");
        content.setPrefWidth(400);

        String nomDoc = doc.getNom() != null ? doc.getNom() : "Dr. " + doc.getUsername();

        Label title = new Label((editMode ? "Modifier la demande avec " : "Demander un RDV avec ") + nomDoc);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label specLabel = new Label(doc.getSpecialite() + " — " + doc.getVille());
        specLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");

        Separator sep = new Separator();

        // Date souhaitée
        Label dateLbl = new Label("Date souhaitée :");
        dateLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Choisir une date");
        datePicker.setPrefWidth(360);
        datePicker.setStyle("-fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-radius: 8;");

        // Heure souhaitée
        Label heureLbl = new Label("Heure souhaitée (HH:mm) :");
        heureLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        TextField heureField = new TextField("09:00");
        heureField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-border-color: #30363d; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        // Motif
        Label motifLbl = new Label("Motif (optionnel) :");
        motifLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        TextField motifField = new TextField();
        motifField.setPromptText("Ex: Consultation, Suivi, Anxiété...");
        motifField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #484f58; -fx-border-color: #30363d; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        Label infoLabel = new Label("ℹ  Le docteur recevra votre demande et pourra l'accepter ou la refuser.");
        infoLabel.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
        infoLabel.setWrapText(true);

        Label statusLbl = new Label("");
        statusLbl.setStyle("-fx-font-size: 13px;");

        if (editMode && existingRequest.getAppointmentDate() != null) {
            datePicker.setValue(existingRequest.getAppointmentDate().toLocalDate());
            heureField.setText(existingRequest.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        content.getChildren().addAll(title, specLabel, sep, dateLbl, datePicker,
                heureLbl, heureField, motifLbl, motifField,
                infoLabel, statusLbl);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1;");

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(editMode ? "Enregistrer" : "Envoyer la demande");
        okBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setText("Annuler");
        cancelBtn.setStyle("-fx-background-color: #21262d; -fx-text-fill: #8b949e; -fx-background-radius: 8;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                LocalDateTime requestedDateTime = parseRequestedDateTime(datePicker, heureField, statusLbl);
                if (requestedDateTime == null) {
                    statusLbl.setText("⚠ Veuillez choisir une date.");
                    return;
                }
                int patientId = SessionManager.getInstance().getUserId();
                boolean success = editMode
                        ? requestDAO.updatePendingRequest(existingRequest.getId(), patientId, doc.getId(), requestedDateTime)
                        : requestDAO.createRequest(patientId, doc.getId(), requestedDateTime);
                Alert alert;
                if (success) {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Demande envoyée");
                    alert.setHeaderText(null);
                    alert.setContentText("✓ Votre demande de RDV avec " + nomDoc +
                            " a été envoyée pour le " + datePicker.getValue() +
                            " à " + heureField.getText() + ".\n\nLe docteur vous confirmera bientôt.");
                } else {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de l'envoi. Réessayez.");
                }
                styleAlert(alert);
                alert.showAndWait();
                loadAppointments();
            }
        });
    }
    private LocalDateTime parseRequestedDateTime(DatePicker datePicker, TextField heureField, Label statusLbl) {
        if (datePicker.getValue() == null) {
            statusLbl.setText("Veuillez choisir une date.");
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(heureField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(datePicker.getValue(), time);
        } catch (DateTimeParseException e) {
            statusLbl.setText("Heure invalide. Utilisez le format HH:mm.");
            return null;
        }
    }

    private void loadAppointments() {
        appointmentsContainer.getChildren().clear();
        int patientId = SessionManager.getInstance().getUserId();
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
        List<AppointmentRequest> pendingRequests = requestDAO.getRequestsByPatient(patientId)
                .stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .toList();

        if (appointments.isEmpty() && pendingRequests.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            appointmentsContainer.getChildren().add(empty);
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd 'à' HH:mm");
        for (AppointmentRequest request : pendingRequests) {
            appointmentsContainer.getChildren().add(createPendingRequestRow(request, fmt));
        }
        for (Appointment appt : appointments) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 16; " +
                    "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");

            // Avatar
            Label av = new Label("👨‍⚕️");
            av.setStyle("-fx-font-size: 28px; -fx-background-color: linear-gradient(#6e40c9, #4a9eff); " +
                    "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");

            // Info
            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label dname = new Label("Dr. " + appt.getDocteurName());
            dname.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label spec = new Label(appt.getSpecialite());
            spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            String dateStr = appt.getDateTime() != null ? appt.getDateTime().format(fmt) : "N/A";
            Label date = new Label("📅 " + dateStr);
            date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
            info.getChildren().addAll(dname, spec, date);

            // Status + Cancel
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER);
            String status = appt.getStatus();
            String statusColor = "Confirmé".equals(status) ? "#00b4ab" : "#f0a500";
            Label statusLbl = new Label(status);
            statusLbl.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;");

            Button cancelBtn = new Button("Annuler");
            cancelBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; -fx-background-radius: 16; " +
                    "-fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 12px;");
            int apptId = appt.getId();
            cancelBtn.setOnAction(e -> {
                appointmentDAO.cancelAppointment(apptId);
                loadAppointments();
            });

            actions.getChildren().addAll(statusLbl, cancelBtn);
            row.getChildren().addAll(av, info, actions);
            appointmentsContainer.getChildren().add(row);
        }
    }

    private HBox createPendingRequestRow(AppointmentRequest request, DateTimeFormatter fmt) {
        docteur doc = serviceDocteur.getById(request.getDocteurId());
        String docName = doc != null ? (doc.getNom() != null ? doc.getNom() : doc.getUsername()) : "Medecin";
        String specialite = doc != null ? doc.getSpecialite() : "";

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 16; " +
                "-fx-border-color: #f0a500; -fx-border-radius: 10; -fx-border-width: 1;");

        Label av = new Label("RDV");
        av.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #f0a500; " +
                "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label dname = new Label("Dr. " + docName);
        dname.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label spec = new Label(specialite);
        spec.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
        String dateStr = request.getAppointmentDate() != null ? request.getAppointmentDate().format(fmt) : "Date demandee non definie";
        Label date = new Label(dateStr);
        date.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        info.getChildren().addAll(dname, spec, date);

        Label badge = new Label("En attente");
        badge.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; " +
                "-fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white; -fx-background-radius: 16; " +
                "-fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 12px;");
        editBtn.setOnAction(e -> {
            if (doc != null) showAppointmentPopup(doc, request);
        });

        Button deleteBtn = new Button("Annuler");
        deleteBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; -fx-background-radius: 16; " +
                "-fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> cancelPendingRequest(request));

        HBox actions = new HBox(8, badge, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        row.getChildren().addAll(av, info, actions);
        return row;
    }

    private void cancelPendingRequest(AppointmentRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la demande");
        confirm.setHeaderText(null);
        confirm.setContentText("Annuler cette demande de rendez-vous ?");
        styleAlert(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            int patientId = SessionManager.getInstance().getUserId();
            boolean ok = requestDAO.deletePendingRequest(request.getId(), patientId);
            if (!ok) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Action impossible. La demande a peut-etre deja ete acceptee.");
                styleAlert(alert);
                alert.showAndWait();
            }
            loadAppointments();
        }
    }

    private void loadPrescriptions() {
        prescriptionsContainer.getChildren().clear();
        int patientId = SessionManager.getInstance().getUserId();
        List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByPatient(patientId);

        if (prescriptions.isEmpty()) {
            Label empty = new Label("Aucune prescription.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            prescriptionsContainer.getChildren().add(empty);
            return;
        }

        for (Prescription p : prescriptions) {
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
            prescriptionsContainer.getChildren().add(row);
        }
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d;");
        Label headerLabel = (Label) alert.getDialogPane().lookup(".header-panel .label");
        if (headerLabel != null) headerLabel.setStyle("-fx-text-fill: white;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
    }
}
