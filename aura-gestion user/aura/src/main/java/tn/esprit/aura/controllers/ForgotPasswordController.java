package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.MailService;
import tn.esprit.aura.utils.NavigationManager;

import java.sql.SQLException;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private Label cardTitle;
    @FXML private Label messageLabel;
    
    @FXML private VBox step1Email;
    @FXML private VBox step2OTP;
    @FXML private VBox step3Password;

    @FXML private TextField emailField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();
    private String generatedOTP;
    private String targetEmail;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        showStep(1);
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage("Veuillez saisir votre adresse email.", true);
            return;
        }

        try {
            if (!userService.emailExists(email)) {
                showMessage("Aucun compte trouve avec cet email.", true);
                return;
            }

            targetEmail = email;
            generatedOTP = String.format("%06d", new Random().nextInt(999999));

            // Send Email in background thread to avoid UI freeze
            new Thread(() -> {
                try {
                    MailService.sendOTP(targetEmail, generatedOTP);
                    javafx.application.Platform.runLater(() -> {
                        showMessage("Un code a 6 chiffres a ete envoye a votre email.", false);
                        showStep(2);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        showMessage("Echec de l envoi de l email. Verifiez votre connexion.", true);
                    });
                    e.printStackTrace();
                }
            }).start();

            showMessage("Envoi du code de verification...", false);

        } catch (SQLException e) {
            showMessage("Erreur de base de donnees. Veuillez reessayer.", true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerifyOTP() {
        String enteredOTP = otpField.getText().trim();

        if (enteredOTP.isEmpty()) {
            showMessage("Veuillez saisir le code de verification.", true);
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            showMessage("Code verifie avec succes !", false);
            showStep(3);
        } else {
            showMessage("Code de verification invalide. Veuillez reessayer.", true);
        }
    }

    @FXML
    private void handleResendOTP() {
        handleSendCode();
    }

    @FXML
    private void handleResetPassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty()) {
            showMessage("Le nouveau mot de passe ne peut pas etre vide.", true);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showMessage("Les mots de passe ne correspondent pas.", true);
            return;
        }

        try {
            userService.updatePassword(targetEmail, newPass);
            showMessage("Mot de passe mis a jour avec succes. Redirection...", false);
            
            // Redirect to login after 2 seconds
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> handleBackToLogin());
            pause.play();

        } catch (SQLException e) {
            showMessage("Erreur lors de la mise a jour du mot de passe. Veuillez reessayer.", true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
    }

    private void showStep(int step) {
        step1Email.setVisible(step == 1);
        step1Email.setManaged(step == 1);
        step2OTP.setVisible(step == 2);
        step2OTP.setManaged(step == 2);
        step3Password.setVisible(step == 3);
        step3Password.setManaged(step == 3);

        if (step == 1) cardTitle.setText("Reinitialiser le mot de passe");
        if (step == 2) cardTitle.setText("Verifier l identite");
        if (step == 3) cardTitle.setText("Creer un nouveau mot de passe");
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #E85D3A;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #1BBFA8;");
        }
    }
}
