package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.AppThemeManager;
import tn.esprit.aura.services.AdaptiveWorkspaceManager;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

public class UserDashboardController {

    @FXML private BorderPane dashboardRoot;
    @FXML private Label userWelcomeLabel;
    @FXML private Label userSidebarNameLabel;
    @FXML private StackPane userContentArea;
    @FXML private HBox topbar;
    @FXML private javafx.scene.layout.VBox userSidebar;
    @FXML private javafx.scene.shape.Circle auraLogoCircle;
    @FXML private javafx.scene.shape.Circle userStatusCircle;

    // Sidebar buttons
    @FXML private Button userBtnTasks;
    @FXML private Button userBtnAnalysis;
    @FXML private Button userBtnActivities;
    @FXML private Button userBtnMedical;
    @FXML private Button userBtnMessages;
    @FXML private Button userBtnEvents;
    @FXML private Button userBtnProfile;

    // Doctor Specific buttons
    @FXML private Button doctorBtnDemandes;
    @FXML private Button doctorBtnPatients;
    @FXML private Button doctorBtnPrescriptions;

    // The active button is now tracked by AdaptiveWorkspaceManager

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

        AppThemeManager.setRoot(dashboardRoot);
        setupSidebar(user.getRole());

        // Register with AdaptiveWorkspaceManager for dynamic themes
        AdaptiveWorkspaceManager.getInstance().register(
                dashboardRoot,
                userSidebar,
                java.util.Arrays.asList(
                        userBtnTasks, userBtnAnalysis, userBtnActivities,
                        userBtnMedical, userBtnMessages, userBtnEvents, userBtnProfile,
                        doctorBtnDemandes, doctorBtnPatients, doctorBtnPrescriptions
                )
        );

        // Listen for mood changes to update circles
        AdaptiveWorkspaceManager.getInstance().addListener(() -> {
            String accent = AdaptiveWorkspaceManager.getInstance().getAccentColor();
            if (auraLogoCircle != null) {
                auraLogoCircle.setFill(javafx.scene.paint.Color.web(accent));
            }
            if (userStatusCircle != null) {
                userStatusCircle.setFill(javafx.scene.paint.Color.web(accent));
            }
        });
        
        // Initial application
        AdaptiveWorkspaceManager.getInstance().appliquerDepuisHumeur("initial");
    }

    private void setupSidebar(String role) {
        boolean isDoctor = "MEDECIN".equalsIgnoreCase(role);

        if (topbar != null) {
            topbar.setVisible(!isDoctor);
            topbar.setManaged(!isDoctor);
        }

        // Hide/Show buttons based on role
        userBtnTasks.setVisible(!isDoctor);
        userBtnTasks.setManaged(!isDoctor);
        userBtnAnalysis.setVisible(!isDoctor);
        userBtnAnalysis.setManaged(!isDoctor);
        userBtnActivities.setVisible(!isDoctor);
        userBtnActivities.setManaged(!isDoctor);
        userBtnMedical.setVisible(!isDoctor);
        userBtnMedical.setManaged(!isDoctor);
        userBtnEvents.setVisible(!isDoctor);
        userBtnEvents.setManaged(!isDoctor);
        
        // Profile button is now visible for both as requested
        userBtnProfile.setVisible(true);
        userBtnProfile.setManaged(true);

        doctorBtnDemandes.setVisible(isDoctor);
        doctorBtnDemandes.setManaged(isDoctor);
        doctorBtnPatients.setVisible(isDoctor);
        doctorBtnPatients.setManaged(isDoctor);
        doctorBtnPrescriptions.setVisible(isDoctor);
        doctorBtnPrescriptions.setManaged(isDoctor);

        // Load initial view
        if (isDoctor) {
            handleMedical(); // Loads doctor_dashboard.fxml
        } else {
            handleAnalysis(); // Loads analysis.fxml
        }
    }

    @FXML private void handleTasks() { loadView("/tn/esprit/aura/views/tasks.fxml"); setActiveButton(userBtnTasks); }
    @FXML private void handleAnalysis() { loadView("/tn/esprit/aura/views/analysis.fxml"); setActiveButton(userBtnAnalysis); }

    private void setActiveButton(Button btn) {
        AdaptiveWorkspaceManager.getInstance().setActiveButton(btn);
        AdaptiveWorkspaceManager.getInstance().appliquerPaletteUI();
    }

    @FXML
    private void handleMedical() {
        String role = SessionManager.getCurrentUser().getRole();
        if ("MEDECIN".equalsIgnoreCase(role)) {
            loadView("/tn/esprit/aura/views/doctor_dashboard.fxml");
            setActiveButton(doctorBtnDemandes);
        } else {
            loadView("/tn/esprit/aura/views/medical_list.fxml");
            setActiveButton(userBtnMedical);
        }
    }

    @FXML private void handleDoctorPatients() {
        loadView("/tn/esprit/aura/views/doctor_patients.fxml");
        setActiveButton(doctorBtnPatients);
    }

    @FXML private void handleDoctorPrescriptions() {
        loadView("/tn/esprit/aura/views/doctor_prescriptions.fxml");
        setActiveButton(doctorBtnPrescriptions);
    }

    @FXML private void handleMessages() {
        loadView("/tn/esprit/aura/views/messages.fxml");
        setActiveButton(userBtnMessages);
    }

    @FXML private void handleEvents() {
        String role = SessionManager.getCurrentUser().getRole();
        if ("ORGANIZER".equalsIgnoreCase(role)) {
            loadView("/tn/esprit/aura/views/EventsView.fxml");
        } else {
            tn.esprit.aura.controllers.UserEventsView userEventsView = new tn.esprit.aura.controllers.UserEventsView();
            userContentArea.getChildren().setAll(userEventsView);
        }
        setActiveButton(userBtnEvents);
    }

    @FXML private void handleProfile() {
        try {
            Parent profileView = NavigationManager.loadFxml("/tn/esprit/aura/views/ProfileEdit.fxml");
            Parent dashboardView = NavigationManager.loadFxml("/tn/esprit/aura/views/user_home.fxml");
            javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(20);
            container.getChildren().addAll(dashboardView, profileView);
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(container);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            userContentArea.getChildren().setAll(scrollPane);
            setActiveButton(userBtnProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleSettings() { loadView("/tn/esprit/aura/views/settings.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    private void loadView(String fxmlPath) {
        Parent view = NavigationManager.loadFxml(fxmlPath);
        if (view != null) userContentArea.getChildren().setAll(view);
    }
}
