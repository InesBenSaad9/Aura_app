package org.example.ui.views.shared;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.MessageDAO;
import org.example.dao.ServiceDocteur;
import org.example.entities.Message;
import org.example.entities.docteur;
import org.example.utils.NotificationService;
import org.example.utils.SessionManager;
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

    private MessageDAO messageDAO = new MessageDAO();
    private ServiceDocteur serviceDocteur = new ServiceDocteur();

    private int    selectedPartnerId   = -1;
    private String selectedPartnerName = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadContacts();
        contactsList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) openConversation(newVal);
        });
    }

    private void loadContacts() {
        int myId = SessionManager.getInstance().getUserId();
        List<String> items = new ArrayList<>();

        List<docteur> allDoctors = serviceDocteur.getAllDocteurs();
        List<Integer> existingConvs = messageDAO.getDocteurPartnersForPatient(myId);

        for (int docId : existingConvs) {
            String name = messageDAO.resolveNameById(docId, "doctor");
            items.add(name + "|" + docId + "|hasMsg");
        }
        for (docteur d : allDoctors) {
            if (!existingConvs.contains(d.getId())) {
                String name = d.getNom() != null ? d.getNom() : d.getUsername();
                items.add(name + "|" + d.getId() + "|noMsg");
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
        if (selectedPartnerId == -1) return;

        int myId = SessionManager.getInstance().getUserId();
        List<Message> messages = messageDAO.getConversation(myId, selectedPartnerId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (Message msg : messages) {
            boolean isMine = "patient".equals(msg.getSenderRole()) && msg.getSenderId() == myId;

            HBox wrapper = new HBox();
            wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            wrapper.setPadding(new Insets(2, 8, 2, 8));

            if (!isMine) {
                Label av = new Label(selectedPartnerName.isEmpty() ? "D" :
                        String.valueOf(selectedPartnerName.charAt(0)).toUpperCase());
                av.setMinWidth(32); av.setMinHeight(32);
                av.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 16; -fx-alignment: center; -fx-font-size: 12px;");
                wrapper.getChildren().add(av);
                HBox.setMargin(av, new Insets(0, 6, 0, 0));
            }

            VBox bubble = new VBox(3);
            bubble.setMaxWidth(380);
            bubble.setPadding(new Insets(10, 14, 8, 14));
            bubble.setStyle("-fx-background-color: " + (isMine ? "#00b4ab" : "#21262d") + "; " +
                    "-fx-background-radius: " + (isMine ? "18 18 4 18" : "18 18 18 4") + ";");

            Label content = new Label(msg.getContent());
            content.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            content.setMaxWidth(360);
            content.setWrapText(true);

            String timeStr = msg.getSentAt() != null ? msg.getSentAt().format(fmt) : "";
            HBox footer = new HBox(4);
            footer.setAlignment(Pos.CENTER_RIGHT);
            Label time = new Label(timeStr);
            time.setStyle("-fx-text-fill: " + (isMine ? "#cde8e7" : "#6e7681") + "; -fx-font-size: 10px;");
            if (isMine) {
                Label check = new Label("✓✓");
                check.setStyle("-fx-text-fill: #cde8e7; -fx-font-size: 10px;");
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

        int myId = SessionManager.getInstance().getUserId();
        boolean ok = messageDAO.sendMessage(myId, "patient", selectedPartnerId, "doctor", text);
        if (ok) {
            // ✅ NOTIFICATION message envoyé
            NotificationService.message(
                    SessionManager.getInstance().getUsername(),
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
}