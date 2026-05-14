package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

public class AdminController {

    @FXML private StackPane contentArea;
    @FXML private Label adminNameLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnProfile;
    @FXML private Button btnSettings;
    @FXML private VBox sidebar;

    private Button activeButton;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            adminNameLabel.setText(SessionManager.getCurrentUser().getFullName());
        }
        // Load dashboard home by default
        loadView("/tn/esprit/aura/views/admin_home.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    private void handleDashboard() {
        loadView("/tn/esprit/aura/views/admin_home.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    private void handleUsers() {
        loadView("/tn/esprit/aura/views/users.fxml");
        setActiveButton(btnUsers);
    }

    @FXML
    private void handleProfile() {
        loadView("/tn/esprit/aura/views/ProfileEdit.fxml");
        setActiveButton(btnProfile);
    }

    @FXML
    private void handleSettings() {
        loadView("/tn/esprit/aura/views/settings.fxml");
        setActiveButton(btnSettings);
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    private void loadView(String fxmlPath) {
        Parent view = NavigationManager.loadFxml(fxmlPath);
        if (view != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Fade in
            view.setOpacity(0);
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(250), view);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }
        button.getStyleClass().add("sidebar-btn-active");
        activeButton = button;
    }
}
