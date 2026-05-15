package org.example.ui.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.utils.SessionManager;
import org.example.utils.NotificationService;
import java.net.URL;
import java.util.ResourceBundle;

public class SidebarView implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label avatarLabel;
    @FXML private Label statusLabel;
    @FXML private Button btnTasks;
    @FXML private Button btnAI;
    @FXML private Button btnActivities;
    @FXML private Button btnMedical;
    @FXML private Button btnMessages;
    @FXML private Button btnEvents;
    @FXML private Button btnProfile;

    private StackPane contentArea;
    private static final String ACTIVE_BUTTON_STYLE =
            "-fx-background-color: #123a37; -fx-text-fill: #24e0ce; -fx-font-size: 16px; -fx-padding: 12 18; " +
                    "-fx-background-radius: 0 14 14 0; -fx-border-color: #24e0ce; -fx-border-width: 0 0 0 4; -fx-cursor: hand;";
    private static final String INACTIVE_BUTTON_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #d7e5f7; -fx-font-size: 16px; -fx-padding: 11 20; " +
                    "-fx-background-radius: 0 14 14 0; -fx-border-color: transparent; -fx-border-width: 0 0 0 4; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager sm = SessionManager.getInstance();
        String username = sm.getUsername();
        usernameLabel.setText(username != null ? username : "User");
        if (username != null && !username.isEmpty()) {
            avatarLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }
        NotificationService.success("Test notification !");
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
        loadMedical();
    }

    private void setActiveButton(Button active) {
        Button[] buttons = {btnTasks, btnAI, btnActivities, btnMedical, btnMessages, btnEvents, btnProfile};
        for (Button button : buttons) {
            if (button != null) button.setStyle(INACTIVE_BUTTON_STYLE);
        }
        if (active != null) active.setStyle(ACTIVE_BUTTON_STYLE);
    }

    @FXML private void navTasks() { setActiveButton(btnTasks); loadPlaceholder("Tasks"); }
    @FXML private void navAI() { setActiveButton(btnAI); loadPlaceholder("AI Analysis"); }
    @FXML private void navActivities() { setActiveButton(btnActivities); loadPlaceholder("Activities"); }

    @FXML
    private void navMedical() {
        setActiveButton(btnMedical);
        loadMedical();
    }

    @FXML
    private void navMessages() {
        setActiveButton(btnMessages);
        loadMessages();
    }

    @FXML private void navEvents() { setActiveButton(btnEvents); loadPlaceholder("Events"); }
    @FXML private void navProfile() { setActiveButton(btnProfile); loadPlaceholder("Profile"); }
    @FXML private void navSettings() { loadPlaceholder("Settings"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMedical() {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/medical_list.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/messages.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            loadPlaceholder("Messages");
        }
    }

    private void loadPlaceholder(String name) {
        if (contentArea == null) return;
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox();
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-background-color: #0d1117;");
        Label lbl = new Label(name);
        lbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 24px;");
        placeholder.getChildren().add(lbl);
        contentArea.getChildren().setAll(placeholder);
    }
}
