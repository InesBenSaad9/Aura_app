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

    @FXML private Button userBtnMedical;
    @FXML private Button userBtnMessages;
    @FXML private Button userBtnEvents;
    @FXML private Button userBtnTasks;

    private Button activeButton;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
            return;
        }
        userWelcomeLabel.setText(user.getFullName());
        userSidebarNameLabel.setText(
                user.getPrenom() != null && !user.getPrenom().isBlank()
                        ? user.getPrenom()
                        : user.getFullName());
        loadView("/tn/esprit/aura/views/analysis.fxml");
        setActiveButton(userBtnAnalysis);
    }

    // ── Existing navigation ──────────────────────────────────

    @FXML private void handleTasks()      { loadView("/tn/esprit/aura/views/tasks.fxml"); setActiveButton(userBtnTasks); }
    @FXML private void handleAnalysis()   { loadView("/tn/esprit/aura/views/analysis.fxml"); setActiveButton(userBtnAnalysis); }
    @FXML private void handleProfile() {
        try {
            Parent profileView = NavigationManager.loadFxml("/tn/esprit/aura/views/ProfileEdit.fxml");
            Parent dashboardView = NavigationManager.loadFxml("/tn/esprit/aura/views/user_home.fxml");
            
            javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(20);
            container.getChildren().addAll(profileView, dashboardView);
            
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(container);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            
            userContentArea.getChildren().setAll(scrollPane);
            setActiveButton(userBtnProfile);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMedical()    { loadView("/tn/esprit/aura/views/user_home.fxml"); setActiveButton(userBtnMedical); }
    @FXML private void handleMessages()   { loadView("/tn/esprit/aura/views/user_home.fxml"); setActiveButton(userBtnMessages); }
    @FXML private void handleEvents() {
        String role = "";
        if (SessionManager.isLoggedIn()) {
            role = SessionManager.getCurrentUser().getRole();
        }
        
        if ("organizateur d'evenement".equalsIgnoreCase(role)) {
            loadView("/tn/esprit/aura/views/EventsView.fxml");
        } else {
            tn.esprit.aura.controllers.UserEventsView userEventsView = new tn.esprit.aura.controllers.UserEventsView();
            userContentArea.getChildren().setAll(userEventsView);
        }
        setActiveButton(userBtnEvents);
    }
    @FXML private void handleSettings()   { loadView("/tn/esprit/aura/views/settings.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    @FXML private void handleGoHome() { NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml"); }


    // ── Helpers ──────────────────────────────────────────────

    private void loadView(String fxmlPath) {
        Parent view = NavigationManager.loadFxml(fxmlPath);
        if (view != null) userContentArea.getChildren().setAll(view);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) activeButton.getStyleClass().remove("user-sidebar-btn-active");
        if (button != null) button.getStyleClass().add("user-sidebar-btn-active");
        activeButton = button;
    }
}
