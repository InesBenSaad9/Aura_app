package tn.esprit.aura.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.application.Platform;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.MailService;

public class RegisterController {
    
    private String faceIdData;

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    @FXML private Label stepSubtitle;
    @FXML private Region progress1;
    @FXML private Region progress2;
    @FXML private Region progress3;
    @FXML private Label globalErrorLabel;
    @FXML private Label successLabel;

    @FXML private javafx.scene.image.ImageView profileImageView;
    @FXML private Label uploadStatusLabel;
    private String uploadedImageUrl = null;

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private Label prenomError;
    @FXML private Label nomError;
    @FXML private Label emailError;

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label phoneError;
    
    @FXML private Label faceIdStatusLabel;
    @FXML private Button btnFaceId;

    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField cityField;
    @FXML private TextArea bioArea;
    @FXML private Label dobError;
    @FXML private Label genreError;
    @FXML private Label cityError;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Homme", "Femme", "Autre");
        resetErrors();
    }

    @FXML
    private void handleNextToStep2() {
        if (validateStep1()) {
            showStep(2);
        }
    }

    @FXML
    private void handleBackToStep1() {
        showStep(1);
    }

    @FXML
    private void handleNextToStep3() {
        if (validateStep2()) {
            showStep(3);
        }
    }

    @FXML
    private void handleBackToStep2() {
        showStep(2);
    }

    @FXML
    private void handleGoToLogin() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
    }

    @FXML
    private void handleGoHome() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Home.fxml");
    }

    @FXML
    private void handleFaceIdSetup() {
        FaceIdRegisterController.returnFxmlPath = null; // Tell it to close its own window when done
        FaceIdRegisterController.onFaceCaptured = (data) -> {
            this.faceIdData = data;
            Platform.runLater(() -> {
                faceIdStatusLabel.setText("Signature biometrique capturee !");
                faceIdStatusLabel.setStyle("-fx-text-fill: #1BBFA8;");
                btnFaceId.setText("📸 Face ID pret");
            });
        };
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/tn/esprit/aura/views/FaceIdRegister.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Configuration Face ID");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            globalErrorLabel.setText("Impossible d ouvrir la configuration Face ID.");
        }
    }

    private void showStep(int step) {
        step1Box.setVisible(step == 1);
        step1Box.setManaged(step == 1);
        step2Box.setVisible(step == 2);
        step2Box.setManaged(step == 2);
        step3Box.setVisible(step == 3);
        step3Box.setManaged(step == 3);

        String active = "-fx-background-color: #1BBFA8; -fx-background-radius: 2;";
        String inactive = "-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 2;";
        progress1.setStyle(step >= 1 ? active : inactive);
        progress2.setStyle(step >= 2 ? active : inactive);
        progress3.setStyle(step == 3 ? active : inactive);

        switch (step) {
            case 1 -> stepSubtitle.setText("Etape 1 sur 3 : informations de base");
            case 2 -> stepSubtitle.setText("Etape 2 sur 3 : securite et contact");
            case 3 -> stepSubtitle.setText("Etape 3 sur 3 : informations personnelles");
            default -> stepSubtitle.setText("Etape 1 sur 3 : informations de base");
        }
        resetErrors();
    }

    private boolean validateStep1() {
        boolean valid = true;
        resetErrors();

        if (prenomField.getText().trim().isEmpty()) {
            showFieldError(prenomError, "Le prenom est obligatoire.");
            valid = false;
        }
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomError, "Le nom est obligatoire.");
            valid = false;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailError, "L email est obligatoire.");
            valid = false;
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(emailError, "Format d email invalide.");
            valid = false;
        } else {
            try {
                if (userService.emailExists(email)) {
                    showFieldError(emailError, "Cet email est deja utilise.");
                    valid = false;
                }
            } catch (Exception e) {
                globalErrorLabel.setText("Erreur de base de donnees pendant la verification de l email.");
            }
        }
        return valid;
    }

    private boolean validateStep2() {
        boolean valid = true;
        resetErrors();

        String pass = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        String phone = phoneField.getText().trim();

        if (pass.length() < 6) {
            showFieldError(passwordError, "Minimum 6 caracteres.");
            valid = false;
        }
        if (!pass.equals(confirm)) {
            showFieldError(confirmPasswordError, "Les mots de passe ne correspondent pas.");
            valid = false;
        }
        if (phone.isEmpty()) {
            showFieldError(phoneError, "Le telephone est obligatoire.");
            valid = false;
        } else if (!phone.matches("^\\d{8}$")) {
            showFieldError(phoneError, "Le telephone doit contenir exactement 8 chiffres.");
            valid = false;
        }
        return valid;
    }

    private boolean validateStep3() {
        boolean valid = true;
        resetErrors();

        if (dobPicker.getValue() == null) {
            showFieldError(dobError, "La date est obligatoire.");
            valid = false;
        }
        if (genreComboBox.getValue() == null) {
            showFieldError(genreError, "Le genre est obligatoire.");
            valid = false;
        }
        if (cityField.getText().trim().isEmpty()) {
            showFieldError(cityError, "La ville est obligatoire.");
            valid = false;
        }
        return valid;
    }

    @FXML
    private void handleUploadImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Selectionner une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers image", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(NavigationManager.getPrimaryStage());

        if (selectedFile != null) {
            uploadStatusLabel.setText("Importation...");
            uploadStatusLabel.setStyle("-fx-text-fill: #FBBF24;");

            new Thread(() -> {
                String url = tn.esprit.aura.utils.CloudinaryService.uploadImage(selectedFile);
                Platform.runLater(() -> {
                    if (url != null) {
                        uploadedImageUrl = url;
                        profileImageView.setImage(new javafx.scene.image.Image(url));
                        uploadStatusLabel.setText("Image importee avec succes !");
                        uploadStatusLabel.setStyle("-fx-text-fill: #1BBFA8;");
                    } else {
                        uploadStatusLabel.setText("Echec de l importation !");
                        uploadStatusLabel.setStyle("-fx-text-fill: #E85D3A;");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleRegister() {
        if (!validateStep3()) {
            return;
        }

        try {
            User user = new User();
            user.setPrenom(prenomField.getText().trim());
            user.setNom(nomField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole("USER");
            user.setMotDePasse(passwordField.getText().trim());
            user.setTelephone(phoneField.getText().trim());
            user.setDateNaissance(dobPicker.getValue());
            user.setGenre(genreComboBox.getValue());
            user.setVille(cityField.getText().trim());
            user.setBio(bioArea.getText().trim());
            user.setFaceData(faceIdData);
            user.setPhotoProfil(uploadedImageUrl);
            user.setActive(true);

            userService.add(user);

            // Send Bienvenue Email in background
            new Thread(() -> {
                try {
                    MailService.sendWelcomeEmail(user.getEmail(), user.getPrenom());
                } catch (Exception ex) {
                    System.err.println("Impossible d envoyer l email de bienvenue : " + ex.getMessage());
                }
            }).start();

            successLabel.setText("Compte cree avec succes !");
            successLabel.setStyle("-fx-text-fill: #1BBFA8;");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml"));
            pause.play();
        } catch (Exception e) {
            globalErrorLabel.setText("Echec de l inscription : " + e.getMessage());
        }
    }

    private void resetErrors() {
        globalErrorLabel.setText("");
        successLabel.setText("");
        hideFieldError(prenomError);
        hideFieldError(nomError);
        hideFieldError(emailError);
        hideFieldError(passwordError);
        hideFieldError(confirmPasswordError);
        hideFieldError(phoneError);
        hideFieldError(dobError);
        hideFieldError(genreError);
        hideFieldError(cityError);
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
