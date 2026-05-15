package tn.esprit.aura.controllers.shared;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.aura.dao.MessageDAO;
import tn.esprit.aura.dao.DoctorDAO;
import tn.esprit.aura.entities.Message;
import tn.esprit.aura.entities.Doctor;
import tn.esprit.aura.utils.NotificationService;
import tn.esprit.aura.utils.SessionManager;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MessagesView implements Initializable {

    @FXML private ListView<String> contactsList;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Label chatHeader;
    @FXML private ScrollPane messagesScroll;

    private final MessageDAO messageDAO = new MessageDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private int    selectedPartnerId   = -1;
    private String selectedPartnerName = "";
    
    public static int preSelectedContactId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messagesScroll.setStyle("-fx-background-color: transparent; -fx-background: #0b141a;");
        messagesScroll.setFitToWidth(true);
        messagesScroll.setFitToHeight(true);
        
        loadContacts();
        contactsList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) openConversation(newVal);
        });
        
        if (preSelectedContactId != -1) {
            for (String item : contactsList.getItems()) {
                if (item.contains("|" + preSelectedContactId + "|")) {
                    contactsList.getSelectionModel().select(item);
                    break;
                }
            }
            preSelectedContactId = -1; // Reset it
        }
    }

    private void loadContacts() {
        int myId = SessionManager.getCurrentUser().getId();
        boolean isDoctor = "MEDECIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
        List<String> items = new ArrayList<>();

        if (isDoctor) {
            // Doctor sees patients — use real docteur ID from docteurs table
            int realDoctorId = getRealDoctorId(myId);
            List<Integer> existingConvs = messageDAO.getPatientPartnersForDoctor(realDoctorId);
            for (int patId : existingConvs) {
                String name = messageDAO.resolveNameById(patId, "patient");
                items.add(name + "|" + patId + "|hasMsg");
            }
            // Also add patients from accepted appointment requests
            try (java.sql.Connection conn = tn.esprit.aura.utils.DBConnection.getInstance().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DISTINCT ar.patient_id, IFNULL(u.nom, CONCAT('Patient #', ar.patient_id)) as pnom " +
                    "FROM appointment_requests ar LEFT JOIN users u ON ar.patient_id = u.id " +
                    "WHERE ar.docteur_id = ? AND ar.status = 'ACCEPTED'")) {
                ps.setInt(1, realDoctorId);
                java.sql.ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int pid = rs.getInt("patient_id");
                    if (!existingConvs.contains(pid)) {
                        items.add(rs.getString("pnom") + "|" + pid + "|noMsg");
                    }
                }
            } catch (Exception e) {
                System.err.println("MessagesView contacts error: " + e.getMessage());
            }
        } else {
            // Patient sees doctors
            List<Doctor> allDoctors = doctorDAO.getAllDoctors();
            List<Integer> existingConvs = messageDAO.getDocteurPartnersForPatient(myId);

            for (int docId : existingConvs) {
                String name = messageDAO.resolveNameById(docId, "doctor");
                items.add(name + "|" + docId + "|hasMsg");
            }
            for (Doctor d : allDoctors) {
                if (!existingConvs.contains(d.getId())) {
                    String name = (d.getNom() != null && !d.getNom().isBlank()) ? d.getNom() : "Doctor";
                    items.add(name + "|" + d.getId() + "|noMsg");
                }
            }
        }

        contactsList.getItems().setAll(items);

        contactsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                String[] parts  = item.split("\\|");
                String   name   = parts[0];
                boolean  hasMsg = parts.length > 2 && "hasMsg".equals(parts[2]);

                HBox cell = new HBox(12);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPadding(new Insets(10, 14, 10, 14));

                Label av = new Label(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
                av.setMinWidth(40); av.setMinHeight(40);
                av.setStyle("-fx-background-color: " + (hasMsg ? "#00b4ab" : "#21262d") + "; " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 20; -fx-alignment: center; -fx-font-size: 15px;");

                VBox info = new VBox(2);
                Label nameLbl = new Label(name);
                nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                Label subLbl = new Label(hasMsg ? "Conversation active" : "Démarrer une conversation");
                subLbl.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 11px;");
                info.getChildren().addAll(nameLbl, subLbl);

                cell.getChildren().addAll(av, info);
                setGraphic(cell);
                setStyle("-fx-background-color: " + (isSelected() ? "#1c2b2b" : "transparent") + "; " +
                        "-fx-border-color: transparent transparent #21262d transparent; -fx-border-width: 1;");
            }
        });
    }

    private void openConversation(String contactItem) {
        String[] parts      = contactItem.split("\\|");
        selectedPartnerName = parts[0];
        selectedPartnerId   = Integer.parseInt(parts[1]);
        chatHeader.setText("💬 " + selectedPartnerName);
        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        messagesContainer.setStyle("-fx-background-color: #0b141a;"); // WhatsApp Dark background
        if (selectedPartnerId == -1) return;

        int myId = SessionManager.getCurrentUser().getId();
        boolean isDoctor = "MEDECIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
        String myRole = isDoctor ? "doctor" : "patient";
        int realSenderId = isDoctor ? getRealDoctorId(myId) : myId;
        
        List<Message> messages;
        if (isDoctor) {
            messages = messageDAO.getConversation(selectedPartnerId, realSenderId);
        } else {
            messages = messageDAO.getConversation(myId, selectedPartnerId);
        }
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (Message msg : messages) {
            boolean isMine = myRole.equals(msg.getSenderRole()) && msg.getSenderId() == realSenderId;

            HBox wrapper = new HBox();
            wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            wrapper.setPadding(new Insets(2, 8, 2, 8));

            if (!isMine) {
                // In WhatsApp, individual messages usually don't have avatars if it's a 1-on-1 chat.
                // We just use a small margin on the left to align things.
                HBox spacer = new HBox();
                spacer.setMinWidth(10);
                wrapper.getChildren().add(spacer);
            }

            VBox bubble = new VBox(3);
            bubble.setMaxWidth(380);
            bubble.setPadding(new Insets(8, 12, 6, 12));
            
            // WhatsApp dark mode bubble colors: Mine = #005c4b, Theirs = #202c33
            String bgColor = isMine ? "#005c4b" : "#202c33";
            // Border radius with the "tail" on top-right for mine, top-left for theirs
            String radius = isMine ? "8 0 8 8" : "0 8 8 8";
            
            bubble.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-background-radius: " + radius + "; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");

            Label content = new Label(msg.getContent());
            content.setStyle("-fx-text-fill: #e9edef; -fx-font-size: 14px;");
            content.setMaxWidth(360);
            content.setWrapText(true);

            String timeStr = msg.getSentAt() != null ? msg.getSentAt().format(fmt) : "";
            HBox footer = new HBox(4);
            footer.setAlignment(Pos.CENTER_RIGHT);
            Label time = new Label(timeStr);
            time.setStyle("-fx-text-fill: #8696a0; -fx-font-size: 10px;");
            
            if (isMine) {
                Label check = new Label("✓✓");
                check.setStyle("-fx-text-fill: #53bdeb; -fx-font-size: 10px;"); // Blue ticks
                footer.getChildren().addAll(time, check);
            } else {
                footer.getChildren().add(time);
            }

            bubble.getChildren().addAll(content, footer);
            wrapper.getChildren().add(bubble);
            messagesContainer.getChildren().add(wrapper);
        }

        messagesScroll.setVvalue(1.0);
        messagesContainer.heightProperty().addListener(o -> messagesScroll.setVvalue(1.0));
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || selectedPartnerId == -1) return;

        int myId = SessionManager.getCurrentUser().getId();
        boolean isDoctor = "MEDECIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
        int senderId = isDoctor ? getRealDoctorId(myId) : myId;
        String myRole = isDoctor ? "doctor" : "patient";
        String partnerRole = isDoctor ? "patient" : "doctor";

        boolean ok = messageDAO.sendMessage(senderId, myRole, selectedPartnerId, partnerRole, text);
        if (ok) {
            // ✅ NOTIFICATION message envoyé
            NotificationService.message(
                    SessionManager.getCurrentUser().getNom(),
                    text
            );
            messageInput.clear();
            loadMessages();
            loadContacts();
            // Resélectionner le contact actif
            for (String item : contactsList.getItems()) {
                if (item.split("\\|")[1].equals(String.valueOf(selectedPartnerId))) {
                    contactsList.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    private int getRealDoctorId(int sessionId) {
        String nom = SessionManager.getCurrentUser().getNom();
        if (nom == null || nom.trim().isEmpty()) return sessionId;
        try (java.sql.Connection conn = tn.esprit.aura.utils.DBConnection.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id FROM docteurs WHERE nom LIKE ?")) {
            ps.setString(1, "%" + nom.trim() + "%");
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            System.err.println("getRealDoctorId error: " + e.getMessage());
        }
        return sessionId;
    }
}
