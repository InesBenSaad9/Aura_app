package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

public class UserDashboardController {

    @FXML private Label userWelcomeLabel;
    @FXML private Label userSidebarNameLabel;
    @FXML private StackPane userContentArea;
    @FXML private Button userBtnAnalysis;
    @FXML private Button userBtnProfile;
    @FXML private Button userBtnActivities;
    @FXML private Button userBtnMedical;
    @FXML private Button userBtnMessages;
    @FXML private Button userBtnEvents;

    private Button activeButton;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
            return;
        }
        userWelcomeLabel.setText(user.getFullName());
        userSidebarNameLabel.setText(user.getPrenom() != null && !user.getPrenom().isBlank() ? user.getPrenom() : user.getFullName());
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnAnalysis);
    }

    @FXML
    private void handleDashboard() {
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnAnalysis);
    }

    @FXML
    private void handleProfile() {
        loadView("/tn/esprit/aura/views/ProfileEdit.fxml");
        setActiveButton(userBtnProfile);
    }

    @FXML
    private void handleActivities() {
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnActivities);
    }

    @FXML
    private void handleMedical() {
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnMedical);
    }

    @FXML
    private void handleMessages() {
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnMessages);
    }

    @FXML
    private void handleEvents() {
        loadView("/tn/esprit/aura/views/user_home.fxml");
        setActiveButton(userBtnEvents);
    }

    @FXML
    private void handleSettings() {
        loadView("/tn/esprit/aura/views/settings.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    @FXML
    private void handleGoHome() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    private void loadView(String fxmlPath) {
        Parent view = NavigationManager.loadFxml(fxmlPath);
        if (view != null) {
            userContentArea.getChildren().setAll(view);
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("user-sidebar-btn-active");
        }
        button.getStyleClass().add("user-sidebar-btn-active");
        activeButton = button;
    }
}
