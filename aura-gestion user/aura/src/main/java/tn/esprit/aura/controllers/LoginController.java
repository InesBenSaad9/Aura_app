package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Label errorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private VBox loginCard;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        hideFieldError(emailErrorLabel);
        hideFieldError(passwordErrorLabel);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordTextField.isVisible() ? passwordTextField.getText() : passwordField.getText();
        password = password.trim();
        clearFieldErrors();
        errorLabel.setText("");

        boolean valid = true;
        if (email.isEmpty()) {
            showFieldError(emailErrorLabel, "L email est obligatoire.");
            valid = false;
        }
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "Le mot de passe est obligatoire.");
            valid = false;
        }
        if (!valid) return;

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(emailErrorLabel, "Veuillez saisir une adresse email valide.");
            return;
        }

        try {
            User user = userService.authenticate(email, password);
            if (user == null) {
                showError("Email ou mot de passe invalide.");
                return;
            }

            SessionManager.setCurrentUser(user);

            if (user.isAdmin()) {
                NavigationManager.navigateTo("/tn/esprit/aura/views/Admin.fxml");
            } else {
                NavigationManager.navigateTo("/tn/esprit/aura/views/UserDashboard.fxml");
            }
        } catch (Exception e) {
            showError("Erreur de connexion. Veuillez reessayer.");
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }

    @FXML
    private void handleGoogleLogin() {
        showError("La connexion Google n est pas encore disponible.");
    }

    @FXML
    private void handleForgotPassword() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/ForgotPassword.fxml");
    }

    @FXML
    private void handleFaceIdLogin() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/FaceIdAuth.fxml");
    }

    @FXML
    private void handleGoToRegister() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Register.fxml");
    }

    @FXML
    private void handleGoHome() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #E85D3A;");
    }

    private void clearFieldErrors() {
        hideFieldError(emailErrorLabel);
        hideFieldError(passwordErrorLabel);
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideFieldError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}
