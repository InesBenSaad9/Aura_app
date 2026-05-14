package org.example.ui.views.doctor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.*;
import org.example.entities.*;
import org.example.utils.NotificationService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorDashboard implements Initializable {

    @FXML private Label doctorNameLabel;
    @FXML private Label doctorAvatarLabel;
    @FXML private Label welcomeLabel;
    @FXML private VBox requestsContainer;

    private AppointmentRequestDAO requestDAO = new AppointmentRequestDAO();
    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private UserDAO userDAO = new UserDAO();
    private MessageDAO messageDAO = new MessageDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String username = SessionManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) username = "Docteur";
        doctorNameLabel.setText(username);
        if (doctorAvatarLabel != null) {
            doctorAvatarLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }
        welcomeLabel.setText("Bienvenue, " + username);
        loadRequests();
    }

    private void loadRequests() {
        requestsContainer.getChildren().clear();
        int docteurId = SessionManager.getInstance().getUserId();
        List<AppointmentRequest> requests = requestDAO.getRequestsByDocteur(docteurId);

        if (requests.isEmpty()) {
            Label empty = new Label("Aucune demande de séance.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            requestsContainer.getChildren().add(empty);
            return;
        }

        for (AppointmentRequest req : requests) {
            requestsContainer.getChildren().add(createRequestRow(req));
        }
    }

    private HBox createRequestRow(AppointmentRequest req) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; " +
                "-fx-border-color: #21262d; -fx-border-radius: 10; -fx-border-width: 1;");

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label patientName = new Label(req.getPatientUsername());
        patientName.setStyle("-fx-text-fill: #00b4ab; -fx-font-size: 14px; -fx-font-weight: bold;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr = req.getCreatedAt() != null ? req.getCreatedAt().format(fmt) : "N/A";
        Label createdAt = new Label("Créé: " + dateStr);
        createdAt.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");

        String status = req.getStatus();
        String statusColor = switch (status) {
            case "ACCEPTED" -> "#00b4ab";
            case "REFUSED"  -> "#f85149";
            default         -> "#f0a500";
        };
        Label statusLbl = new Label("Status: " + status);
        statusLbl.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        info.getChildren().addAll(patientName, createdAt, statusLbl);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        if ("PENDING".equals(status)) {
            Button acceptBtn = new Button("✓ Accepter");
            acceptBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12px;");
            acceptBtn.setOnAction(e -> showAcceptDialog(req));

            Button refuseBtn = new Button("✗ Refuser");
            refuseBtn.setStyle("-fx-background-color: #8b2020; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12px;");
            refuseBtn.setOnAction(e -> {
                requestDAO.updateStatus(req.getId(), "REFUSED");
                // ✅ NOTIFICATION refus
                NotificationService.appointmentRefused(req.getPatientUsername());
                loadRequests();
            });

            actions.getChildren().addAll(acceptBtn, refuseBtn);
        }

        row.getChildren().addAll(userIcon, info, actions);
        return row;
    }

    private void showAcceptDialog(AppointmentRequest req) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Accepter le RDV");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #161b22;");
        content.setPrefWidth(360);

        Label title = new Label("Accepter la demande de " + req.getPatientUsername());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label dateLbl = new Label("Date du rendez-vous:");
        dateLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");

        DatePicker datePicker = new DatePicker();
        datePicker.setStyle("-fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-radius: 8;");
        datePicker.setPromptText("Sélectionner une date");
        datePicker.setPrefWidth(320);

        Label timeLbl = new Label("Heure (HH:mm):");
        timeLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");

        TextField timeField = new TextField("14:00");
        timeField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-border-color: #30363d; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        content.getChildren().addAll(title, dateLbl, datePicker, timeLbl, timeField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d;");

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Confirmer");
        okBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #21262d; -fx-text-fill: #8b949e; -fx-background-radius: 8;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && datePicker.getValue() != null) {
                try {
                    String[] timeParts = timeField.getText().split(":");
                    LocalDateTime dt = datePicker.getValue().atTime(
                            Integer.parseInt(timeParts[0]),
                            Integer.parseInt(timeParts[1])
                    );
                    boolean success = requestDAO.acceptAndSchedule(req.getId(), req.getPatientId(),
                            SessionManager.getInstance().getUserId(), dt);
                    if (success) {
                        // ✅ NOTIFICATION acceptation
                        NotificationService.appointmentAccepted(
                                req.getPatientUsername(),
                                datePicker.getValue().toString()
                        );
                        showAlert(Alert.AlertType.INFORMATION, "RDV confirmé avec succès !");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur lors de la confirmation.");
                    }
                    loadRequests();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Format d'heure invalide. Utilisez HH:mm");
                }
            }
        });
    }

    @FXML private void showDemandes() { loadRequests(); }

    @FXML
    private void showPatients() {
        int docteurId = SessionManager.getInstance().getUserId();

        Stage popup = new Stage();
        popup.setTitle("Mes Patients");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #161b22;");
        root.setPrefWidth(500);

        Label title = new Label("Mes Patients");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        List<AppointmentRequest> accepted = requestDAO.getRequestsByDocteur(docteurId)
                .stream().filter(r -> "ACCEPTED".equals(r.getStatus())).toList();

        java.util.Set<Integer> seen = new java.util.HashSet<>();
        VBox patientsList = new VBox(8);
        for (AppointmentRequest r : accepted) {
            if (seen.add(r.getPatientId())) {
                HBox patRow = new HBox(12);
                patRow.setAlignment(Pos.CENTER_LEFT);
                patRow.setStyle("-fx-background-color: #0d1117; -fx-background-radius: 8; -fx-padding: 12;");

                Label icon = new Label("👤");
                icon.setStyle("-fx-font-size: 18px;");
                Label name = new Label(r.getPatientUsername());
                name.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                HBox.setHgrow(name, Priority.ALWAYS);

                Button msgBtn = new Button("💬 Message");
                msgBtn.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-background-radius: 8; " +
                        "-fx-padding: 6 12; -fx-cursor: hand;");
                int patId = r.getPatientId();
                String patName = r.getPatientUsername();
                msgBtn.setOnAction(e -> openMessageWith(patId, patName, popup));

                patRow.getChildren().addAll(icon, name, msgBtn);
                patientsList.getChildren().add(patRow);
            }
        }

        if (patientsList.getChildren().isEmpty()) {
            Label empty = new Label("Aucun patient accepté.");
            empty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");
            patientsList.getChildren().add(empty);
        }

        root.getChildren().addAll(title, patientsList);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #161b22; -fx-background: #161b22;");
        popup.setScene(new Scene(sp, 520, 450));
        popup.show();
    }

    private void openMessageWith(int patientId, String patientName, Stage parentPopup) {
        if (parentPopup != null) parentPopup.close();

        int myDocId = SessionManager.getInstance().getUserId();

        Stage msgStage = new Stage();
        msgStage.setTitle("Messages — " + patientName);
        msgStage.initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #161b22;");
        root.setPrefSize(860, 600);

        // ── Panel gauche ──────────────────────────────────────────────────────
        VBox contactsPanel = new VBox(0);
        contactsPanel.setPrefWidth(240);
        contactsPanel.setStyle("-fx-background-color: #0d1117; -fx-border-color: #21262d; -fx-border-width: 0 1 0 0;");

        Label convTitle = new Label("💬 Conversations");
        convTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 16;");
        contactsPanel.getChildren().add(convTitle);

        List<Integer> patientIds = messageDAO.getPatientPartnersForDocteur(myDocId);
        List<AppointmentRequest> accepted = requestDAO.getRequestsByDocteur(myDocId)
                .stream().filter(r -> "ACCEPTED".equals(r.getStatus())).toList();
        for (AppointmentRequest r : accepted) {
            if (!patientIds.contains(r.getPatientId())) patientIds.add(r.getPatientId());
        }

        final int[]    activePatientId   = {patientId};
        final String[] activePatientName = {patientName};
        final VBox[]   chatMsgBox        = {null};
        final ScrollPane[] chatScroll    = {null};

        ListView<String> patientListView = new ListView<>();
        VBox.setVgrow(patientListView, Priority.ALWAYS);
        patientListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        for (int pid : patientIds) {
            try {
                java.sql.PreparedStatement ps = DatabaseConnection.getConnection()
                        .prepareStatement("SELECT username FROM users WHERE id = ?");
                ps.setInt(1, pid);
                java.sql.ResultSet rs = ps.executeQuery();
                String name = rs.next() ? rs.getString("username") : "Patient " + pid;
                patientListView.getItems().add(name + "|" + pid);
            } catch (Exception e) { e.printStackTrace(); }
        }

        patientListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String name = item.split("\\|")[0];
                HBox cell = new HBox(10);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPadding(new Insets(10, 14, 10, 14));
                Label av = new Label(String.valueOf(name.charAt(0)).toUpperCase());
                av.setMinWidth(36); av.setMinHeight(36);
                av.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 18; -fx-alignment: center;");
                Label n = new Label(name);
                n.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                cell.getChildren().addAll(av, n);
                setGraphic(cell);
                setStyle("-fx-background-color: " + (isSelected() ? "#1c2b2b" : "transparent") + ";");
            }
        });

        contactsPanel.getChildren().add(patientListView);
        root.setLeft(contactsPanel);

        // ── Panel droit : chat ────────────────────────────────────────────────
        VBox chatPanel = new VBox(0);
        chatPanel.setStyle("-fx-background-color: #161b22;");

        Label chatTitle = new Label("💬 Chat avec " + patientName);
        chatTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 16; " +
                "-fx-border-color: transparent transparent #21262d transparent; -fx-border-width: 1;");

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #161b22;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        chatScroll[0] = sp;

        VBox msgBox = new VBox(6);
        msgBox.setPadding(new Insets(14));
        sp.setContent(msgBox);
        chatMsgBox[0] = msgBox;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        Runnable reloadMessages = () -> {
            msgBox.getChildren().clear();
            List<Message> msgs = messageDAO.getConversation(activePatientId[0], myDocId);
            for (Message m : msgs) {
                boolean mine = "doctor".equals(m.getSenderRole()) && m.getSenderId() == myDocId;
                HBox wrap = new HBox();
                wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                wrap.setPadding(new Insets(2, 8, 2, 8));

                if (!mine) {
                    Label av = new Label(activePatientName[0].isEmpty() ? "P" :
                            String.valueOf(activePatientName[0].charAt(0)).toUpperCase());
                    av.setMinWidth(32); av.setMinHeight(32);
                    av.setStyle("-fx-background-color: #00b4ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-background-radius: 16; -fx-alignment: center; -fx-font-size: 12px;");
                    wrap.getChildren().add(av);
                    HBox.setMargin(av, new Insets(0, 6, 0, 0));
                }

                VBox bubble = new VBox(3);
                bubble.setMaxWidth(380);
                bubble.setPadding(new Insets(10, 14, 8, 14));
                bubble.setStyle("-fx-background-color: " + (mine ? "#1f6feb" : "#21262d") + "; " +
                        "-fx-background-radius: " + (mine ? "18 18 4 18" : "18 18 18 4") + ";");
                Label cl = new Label(m.getContent());
                cl.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                cl.setWrapText(true); cl.setMaxWidth(360);
                String t = m.getSentAt() != null ? m.getSentAt().format(fmt) : "";
                HBox footer = new HBox(4);
                footer.setAlignment(Pos.CENTER_RIGHT);
                Label tl = new Label(t);
                tl.setStyle("-fx-text-fill: " + (mine ? "#aac8ff" : "#6e7681") + "; -fx-font-size: 10px;");
                if (mine) {
                    Label chk = new Label("✓✓");
                    chk.setStyle("-fx-text-fill: #aac8ff; -fx-font-size: 10px;");
                    footer.getChildren().addAll(tl, chk);
                } else { footer.getChildren().add(tl); }
                bubble.getChildren().addAll(cl, footer);
                wrap.getChildren().add(bubble);
                msgBox.getChildren().add(wrap);
            }
            sp.setVvalue(1.0);
        };

        reloadMessages.run();
        msgBox.heightProperty().addListener(o -> sp.setVvalue(1.0));

        patientListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                String[] parts = newVal.split("\\|");
                activePatientName[0] = parts[0];
                activePatientId[0]   = Integer.parseInt(parts[1]);
                chatTitle.setText("💬 Chat avec " + activePatientName[0]);
                reloadMessages.run();
            }
        });

        // ── Input ─────────────────────────────────────────────────────────────
        HBox inputRow = new HBox(10);
        inputRow.setPadding(new Insets(12, 16, 12, 16));
        inputRow.setStyle("-fx-background-color: #0d1117; -fx-border-color: #21262d transparent transparent transparent; -fx-border-width: 1;");
        TextField input = new TextField();
        input.setPromptText("Écrivez votre message...");
        input.setStyle("-fx-background-color: #161b22; -fx-text-fill: white; -fx-prompt-text-fill: #484f58; " +
                "-fx-border-color: #30363d; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 10 14;");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button sendBtn = new Button("Envoyer");
        sendBtn.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");

        sendBtn.setOnAction(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty() && activePatientId[0] != -1) {
                messageDAO.sendMessage(myDocId, "doctor", activePatientId[0], "patient", text);
                // ✅ NOTIFICATION message envoyé
                NotificationService.message(
                        SessionManager.getInstance().getUsername(),
                        text
                );
                input.clear();
                reloadMessages.run();
            }
        });

        input.setOnAction(e -> sendBtn.fire());

        inputRow.getChildren().addAll(input, sendBtn);
        chatPanel.getChildren().addAll(chatTitle, sp, inputRow);
        root.setCenter(chatPanel);

        msgStage.setScene(new Scene(root, 860, 580));
        msgStage.show();
    }

    @FXML
    private void showPrescriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/doctor_prescriptions.fxml"));
            Parent root = loader.load();
            Stage popup = new Stage();
            popup.setTitle("Prescriptions");
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(root, 1050, 700));
            popup.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Impossible d'ouvrir les prescriptions : " + e.getMessage());
        }
    }

    @FXML
    private void showMessages() {
        openMessageWith(-1, "Patient", null);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d;");
        alert.showAndWait();
    }
}