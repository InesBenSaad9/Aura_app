package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

public class ProfileController {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label phoneLabel;
    @FXML private Label cityLabel;
    @FXML private Label genderLabel;
    @FXML private Label bioLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
            return;
        }

        fullNameLabel.setText(emptyAsDash(user.getFullName()));
        emailLabel.setText(emptyAsDash(user.getEmail()));
        roleLabel.setText(emptyAsDash(user.getRole()));
        phoneLabel.setText(emptyAsDash(user.getTelephone()));
        cityLabel.setText(emptyAsDash(user.getVille()));
        genderLabel.setText(emptyAsDash(user.getGenre()));
        bioLabel.setText(emptyAsDash(user.getBio()));
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

    @FXML
    private void handleSetupFaceId() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/FaceIdRegister.fxml");
    }

    private String emptyAsDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
