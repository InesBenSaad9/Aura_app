package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

import java.io.File;
import java.sql.SQLException;

public class ProfileEditController {
    @FXML private Label statusLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label nomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label cityErrorLabel;
    @FXML private Label genderErrorLabel;
    @FXML private Label dobErrorLabel;
    @FXML private Label bioErrorLabel;
    @FXML private Label photoErrorLabel;
    @FXML private Label currentPasswordErrorLabel;
    @FXML private Label newPasswordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private ImageView profileImageView;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cityField;
    @FXML private TextField genderField;
    @FXML private DatePicker dobPicker;
    @FXML private TextArea bioField;
    @FXML private TextField photoPathField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox passwordSection;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
            return;
        }
        fillForm(currentUser);
    }

    @FXML
    private void handleUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selectionner une image de profil");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers image", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File file = chooser.showOpenDialog(NavigationManager.getPrimaryStage());
        if (file != null) {
            photoPathField.setText(file.getAbsolutePath());
            loadProfileImage(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) {
            return;
        }
        clearFieldErrors();

        String email = emailField.getText().trim();
        boolean valid = true;
        if (prenomField.getText().trim().isEmpty()) {
            showFieldError(prenomErrorLabel, "Le prenom est obligatoire.");
            valid = false;
        }
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomErrorLabel, "Le nom est obligatoire.");
            valid = false;
        }
        if (email.isEmpty()) {
            showFieldError(emailErrorLabel, "L email est obligatoire.");
            valid = false;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
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
        if (genderField.getText().trim().isEmpty()) {
            showFieldError(genderErrorLabel, "Le genre est obligatoire.");
            valid = false;
        }
        if (dobPicker.getValue() == null) {
            showFieldError(dobErrorLabel, "La date de naissance est obligatoire.");
            valid = false;
        }
        if (!valid) {
            return;
        }

        try {
            User existing = userService.getByEmail(email);
            if (existing != null && existing.getId() != currentUser.getId()) {
                setError("Cet email est deja utilise par un autre compte.");
                return;
            }

            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setNom(nomField.getText().trim());
            currentUser.setEmail(email);
            currentUser.setTelephone(phoneField.getText().trim());
            currentUser.setVille(cityField.getText().trim());
            currentUser.setGenre(genderField.getText().trim());
            currentUser.setDateNaissance(dobPicker.getValue());
            currentUser.setBio(bioField.getText().trim());
            currentUser.setPhotoProfil(photoPathField.getText().trim());

            userService.update(currentUser);
            SessionManager.setCurrentUser(currentUser);
            setSuccess("Profil mis a jour avec succes.");
        } catch (SQLException e) {
            setError("Echec de la mise a jour du profil : " + e.getMessage());
        }
    }

    @FXML
    private void handleTogglePasswordSection() {
        boolean showing = passwordSection.isVisible();
        passwordSection.setVisible(!showing);
        passwordSection.setManaged(!showing);
        if (showing) {
            clearPasswordFields();
        }
    }

    @FXML
    private void handleResetPassword() {
        if (currentUser == null) {
            return;
        }
        hideFieldError(currentPasswordErrorLabel);
        hideFieldError(newPasswordErrorLabel);
        hideFieldError(confirmPasswordErrorLabel);
        if (!currentPasswordField.getText().equals(currentUser.getMotDePasse())) {
            showFieldError(currentPasswordErrorLabel, "Le mot de passe actuel est incorrect.");
            return;
        }
        if (newPasswordField.getText().length() < 8) {
            showFieldError(newPasswordErrorLabel, "Le nouveau mot de passe doit contenir au moins 8 caracteres.");
            return;
        }
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showFieldError(confirmPasswordErrorLabel, "Le nouveau mot de passe et sa confirmation ne correspondent pas.");
            return;
        }

        try {
            currentUser.setMotDePasse(newPasswordField.getText());
            userService.update(currentUser);
            SessionManager.setCurrentUser(currentUser);
            clearPasswordFields();
            passwordSection.setVisible(false);
            passwordSection.setManaged(false);
            setSuccess("Mot de passe reinitialise avec succes.");
        } catch (SQLException e) {
            setError("Echec de la reinitialisation du mot de passe : " + e.getMessage());
        }
    }

    private void fillForm(User user) {
        prenomField.setText(defaultString(user.getPrenom()));
        nomField.setText(defaultString(user.getNom()));
        emailField.setText(defaultString(user.getEmail()));
        phoneField.setText(defaultString(user.getTelephone()));
        cityField.setText(defaultString(user.getVille()));
        genderField.setText(defaultString(user.getGenre()));
        dobPicker.setValue(user.getDateNaissance());
        bioField.setText(defaultString(user.getBio()));
        photoPathField.setText(defaultString(user.getPhotoProfil()));
        loadProfileImage(user.getPhotoProfil());
        statusLabel.setText("");
        passwordSection.setVisible(false);
        passwordSection.setManaged(false);
        clearFieldErrors();
    }

    private void loadProfileImage(String imagePath) {
        String path = defaultString(imagePath);
        if (path.isBlank()) {
            profileImageView.setImage(null);
            return;
        }
        try {
            File file = new File(path);
            if (file.exists()) {
                profileImageView.setImage(new Image(file.toURI().toString(), true));
            } else {
                profileImageView.setImage(null);
            }
        } catch (Exception e) {
            profileImageView.setImage(null);
        }
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private void setSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #1BBFA8;");
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #E85D3A;");
    }

    private void clearFieldErrors() {
        hideFieldError(prenomErrorLabel);
        hideFieldError(nomErrorLabel);
        hideFieldError(emailErrorLabel);
        hideFieldError(phoneErrorLabel);
        hideFieldError(cityErrorLabel);
        hideFieldError(genderErrorLabel);
        hideFieldError(dobErrorLabel);
        hideFieldError(bioErrorLabel);
        hideFieldError(photoErrorLabel);
        hideFieldError(currentPasswordErrorLabel);
        hideFieldError(newPasswordErrorLabel);
        hideFieldError(confirmPasswordErrorLabel);
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
