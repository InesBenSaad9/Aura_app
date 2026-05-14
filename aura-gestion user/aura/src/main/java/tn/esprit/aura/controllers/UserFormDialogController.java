package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.aura.entities.User;

public class UserFormDialogController {

    @FXML private Label formTitleLabel;
    @FXML private Label errorLabel;
    @FXML private TextField prenomField;
    @FXML private Label prenomErrorLabel;
    @FXML private TextField nomField;
    @FXML private Label nomErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label roleErrorLabel;
    @FXML private TextField cityField;
    @FXML private Label cityErrorLabel;
    @FXML private TextField phoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordErrorLabel;

    private User existingUser;
    private User resultUser;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("USER", "ADMIN");
        roleBox.setValue("USER");
        clearFieldErrors();
    }

    public void setUser(User user) {
        this.existingUser = user;
        if (user == null) {
            formTitleLabel.setText("Ajouter un utilisateur");
            passwordField.setPromptText("Mot de passe");
            return;
        }

        formTitleLabel.setText("Modifier l utilisateur");
        prenomField.setText(safe(user.getPrenom()));
        nomField.setText(safe(user.getNom()));
        emailField.setText(safe(user.getEmail()));
        roleBox.setValue(safe(user.getRole()).isBlank() ? "USER" : user.getRole());
        cityField.setText(safe(user.getVille()));
        phoneField.setText(safe(user.getTelephone()));
        passwordField.setPromptText("Laisser vide pour conserver le mot de passe actuel");
    }

    public User getResultUser() {
        return resultUser;
    }

    @FXML
    private void handleSave() {
        clearFieldErrors();
        boolean valid = true;
        if (prenomField.getText().trim().isEmpty()) {
            showFieldError(prenomErrorLabel, "Le prenom est obligatoire.");
            valid = false;
        }
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomErrorLabel, "Le nom est obligatoire.");
            valid = false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showFieldError(emailErrorLabel, "L email est obligatoire.");
            valid = false;
        } else if (!emailField.getText().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(emailErrorLabel, "Format d email invalide.");
            valid = false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showFieldError(phoneErrorLabel, "Le telephone est obligatoire.");
            valid = false;
        } else if (!phoneField.getText().trim().matches("^\\d{8}$")) {
            showFieldError(phoneErrorLabel, "Le telephone doit contenir exactement 8 chiffres.");
            valid = false;
        }
        if (cityField.getText().trim().isEmpty()) {
            showFieldError(cityErrorLabel, "La ville est obligatoire.");
            valid = false;
        }
        if (roleBox.getValue() == null || roleBox.getValue().isBlank()) {
            showFieldError(roleErrorLabel, "Le role est obligatoire.");
            valid = false;
        }
        if (!valid) return;

        User user = new User();
        user.setPrenom(prenomField.getText().trim());
        user.setNom(nomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setRole(roleBox.getValue());
        user.setVille(cityField.getText().trim());
        user.setTelephone(phoneField.getText().trim());
        user.setBio(existingUser != null ? safe(existingUser.getBio()) : "");
        user.setPhotoProfil(existingUser != null ? safe(existingUser.getPhotoProfil()) : "");
        user.setGenre(existingUser != null ? safe(existingUser.getGenre()) : "");
        user.setDateNaissance(existingUser != null ? existingUser.getDateNaissance() : null);
        user.setActive(existingUser == null || existingUser.isActive());
        user.setMotDePasse(resolvePassword());

        if (user.getMotDePasse().isBlank()) {
            showFieldError(passwordErrorLabel, "Le mot de passe est obligatoire pour les nouveaux utilisateurs.");
            return;
        }
        if (user.getMotDePasse().length() < 8) {
            showFieldError(passwordErrorLabel, "Le mot de passe doit contenir au moins 8 caracteres.");
            return;
        }

        resultUser = user;
        closeStage();
    }

    @FXML
    private void handleCancel() {
        resultUser = null;
        closeStage();
    }

    private String resolvePassword() {
        String entered = passwordField.getText().trim();
        if (!entered.isBlank()) {
            return entered;
        }
        return existingUser != null ? safe(existingUser.getMotDePasse()) : "";
    }

    private void hideGlobalError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearFieldErrors() {
        hideGlobalError();
        hideFieldError(prenomErrorLabel);
        hideFieldError(nomErrorLabel);
        hideFieldError(emailErrorLabel);
        hideFieldError(roleErrorLabel);
        hideFieldError(cityErrorLabel);
        hideFieldError(phoneErrorLabel);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void closeStage() {
        Stage stage = (Stage) formTitleLabel.getScene().getWindow();
        stage.close();
    }
}
