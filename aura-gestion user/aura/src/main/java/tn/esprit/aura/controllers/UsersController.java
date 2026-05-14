package tn.esprit.aura.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.NavigationManager;
import javafx.stage.FileChooser;
import java.io.File;
import tn.esprit.aura.utils.ExportService;
import tn.esprit.aura.utils.QrCodeService;
import tn.esprit.aura.utils.MailService;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UsersController {

    @FXML private TilePane usersCardsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private Label emptyLabel;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();
    private final QrCodeService qrCodeService = new QrCodeService();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roleFilter.getItems().addAll("Tous les roles", "ADMIN", "USER");
        roleFilter.setValue("Tous les roles");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshCards());
        roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshCards());
        loadUsers();
    }

    private void loadUsers() {
        try {
            usersList.clear();
            usersList.addAll(userService.getAll());
            statusLabel.setText(usersList.size() + " utilisateurs charges");
            statusLabel.setStyle("-fx-text-fill: #1BBFA8;");
            refreshCards();
        } catch (SQLException e) {
            statusLabel.setText("Erreur lors du chargement des utilisateurs");
            statusLabel.setStyle("-fx-text-fill: #E85D3A;");
        }
    }

    @FXML
    private void handleAddUserCard() {
        User user = openUserFormDialog(null);
        if (user != null) {
            try {
                if (userService.emailExists(user.getEmail())) {
                    showAlert("Erreur", "Cet email existe deja.");
                    return;
                }
                userService.add(user);

                // Send Bienvenue Email in background
                new Thread(() -> {
                    try {
                        MailService.sendWelcomeEmail(user.getEmail(), user.getPrenom());
                    } catch (Exception ex) {
                        System.err.println("Impossible d envoyer l email de bienvenue : " + ex.getMessage());
                    }
                }).start();

                loadUsers();
                statusLabel.setText("Utilisateur ajoute avec succes");
                statusLabel.setStyle("-fx-text-fill: #1BBFA8;");
            } catch (SQLException e) {
                showAlert("Erreur", "Echec de l ajout de l utilisateur : " + e.getMessage());
            }
        }
    }

    private void refreshCards() {
        usersCardsPane.getChildren().clear();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedRole = roleFilter.getValue();

        List<User> filtered = usersList.stream()
                .filter(user -> "Tous les roles".equals(selectedRole) || selectedRole == null || selectedRole.equalsIgnoreCase(user.getRole()))
                .filter(user -> {
                    if (query.isEmpty()) {
                        return true;
                    }
                    String haystack = (user.getFullName() + " " + user.getEmail() + " " + safe(user.getVille())).toLowerCase();
                    return haystack.contains(query);
                })
                .collect(Collectors.toList());

        for (User user : filtered) {
            usersCardsPane.getChildren().add(createUserCard(user));
        }

        boolean empty = filtered.isEmpty();
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
        statusLabel.setText(filtered.size() + " resultat(s)");
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(350);
        card.setMaxWidth(400);
        card.setStyle("-fx-padding: 16;");

        Label roleBadge = new Label(user.getRole());
        roleBadge.getStyleClass().add("ADMIN".equalsIgnoreCase(user.getRole()) ? "badge-admin" : "badge-user");

        HBox mainContent = new HBox(12);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        VBox textInfo = new VBox(4);
        Label name = new Label(user.getFullName());
        name.getStyleClass().add("topbar-admin-name");
        Label email = new Label(user.getEmail());
        email.getStyleClass().add("page-subtitle");
        Label details = new Label("Ville: " + safe(user.getVille()) + "\nTelephone : " + safe(user.getTelephone()));
        details.getStyleClass().add("stat-card-desc");
        textInfo.getChildren().addAll(name, email, details);
        javafx.scene.layout.HBox.setHgrow(textInfo, javafx.scene.layout.Priority.ALWAYS);

        ImageView qrView = new ImageView();
        try {
            qrView.setImage(qrCodeService.generateUserQrImage(user, 80));
            qrView.setFitWidth(80);
            qrView.setFitHeight(80);
            qrView.setCursor(javafx.scene.Cursor.HAND);
            
            // Adding a nice white background and padding for the QR to stand out on dark themes
            javafx.scene.layout.StackPane qrContainer = new javafx.scene.layout.StackPane(qrView);
            qrContainer.setStyle("-fx-background-color: white; -fx-padding: 4; -fx-border-radius: 4; -fx-background-radius: 4;");
            qrContainer.setCursor(javafx.scene.Cursor.HAND);
            qrContainer.setOnMouseClicked(e -> handleShowQr(user));
            
            mainContent.getChildren().addAll(textInfo, qrContainer);
        } catch (Exception e) {
            mainContent.getChildren().addAll(textInfo);
        }

        Button editBtn = new Button("✏ Modifier");
        Button deleteBtn = new Button("🗑 Supprimer");
        Button pdfBtn = new Button("📄 PDF");
        
        editBtn.getStyleClass().addAll("action-btn", "edit-btn");
        deleteBtn.getStyleClass().addAll("action-btn", "delete-btn");
        pdfBtn.getStyleClass().addAll("action-btn", "edit-btn");
        
        editBtn.setOnAction(e -> handleEditUser(user));
        deleteBtn.setOnAction(e -> handleDeleteUser(user));
        pdfBtn.setOnAction(e -> handleExportPdf(user));
        
        HBox actions = new HBox(8, pdfBtn, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(roleBadge, mainContent, actions);
        return card;
    }

    private void handleEditUser(User user) {
        User updatedUser = openUserFormDialog(user);
        if (updatedUser != null) {
            try {
                updatedUser.setId(user.getId());
                updatedUser.setActive(user.isActive());
                userService.update(updatedUser);
                loadUsers();
                statusLabel.setText("Utilisateur mis a jour avec succes");
                statusLabel.setStyle("-fx-text-fill: #1BBFA8;");
            } catch (SQLException e) {
                showAlert("Erreur", "Echec de la mise a jour de l utilisateur : " + e.getMessage());
            }
        }
    }

    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l utilisateur");
        confirm.setHeaderText("Supprimer " + user.getFullName() + "?");
        confirm.setContentText("Cette action est definitive.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(user.getId());
                    loadUsers();
                    statusLabel.setText("Utilisateur supprime");
                    statusLabel.setStyle("-fx-text-fill: #E85D3A;");
                } catch (SQLException e) {
                    showAlert("Erreur", "Echec de la suppression de l utilisateur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
        fileChooser.setInitialFileName("AURA_Users.xlsx");
        File file = fileChooser.showSaveDialog(NavigationManager.getPrimaryStage());

        if (file != null) {
            try {
                ExportService.exportUsersToExcel(usersList, file.getAbsolutePath());
                showAlert("Succes", "Fichier Excel exporte avec succes !");
            } catch (Exception e) {
                showAlert("Export Erreur", "Echec de l export Excel : " + e.getMessage());
            }
        }
    }

    private void handleExportPdf(User user) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la fiche PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("UserCard_" + user.getFullName().replaceAll("\\s+", "_") + ".pdf");
        File file = fileChooser.showSaveDialog(NavigationManager.getPrimaryStage());

        if (file != null) {
            try {
                ExportService.exportUserToPDF(user, file.getAbsolutePath());
                showAlert("Succes", "Fiche PDF exportee avec succes !");
            } catch (Exception e) {
                showAlert("Export Erreur", "Echec de l export PDF : " + e.getMessage());
            }
        }
    }

    private void handleShowQr(User user) {
        try {
            javafx.scene.image.Image qrImage = qrCodeService.generateUserQrImage(user, 300);
            ImageView imageView = new ImageView(qrImage);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Scanner le QR code");
            alert.setHeaderText("Vue mobile du profil de " + user.getFullName());
            
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                imageView,
                new Label("Scannez ce code avec la camera de votre telephone\npour voir le profil sur mobile.")
            );
            
            alert.getDialogPane().setContent(content);
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Erreur", "Echec de la generation du QR code.");
        }
    }

    private User openUserFormDialog(User existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/aura/views/UserFormDialog.fxml"));
            Parent root = loader.load();
            UserFormDialogController controller = loader.getController();
            controller.setUser(existing);

            Stage stage = new Stage();
            stage.initOwner(NavigationManager.getPrimaryStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(existing == null ? "Ajouter un utilisateur" : "Modifier l utilisateur");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/tn/esprit/aura/styles/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
            return controller.getResultUser();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d ouvrir le formulaire utilisateur : " + e.getMessage());
            return null;
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
