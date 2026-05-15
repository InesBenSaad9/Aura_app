package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserHomeController {

    @FXML private VBox welcomeCard;
    @FXML private Label welcomeNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label emailLabel;
    @FXML private Label cityLabel;
    @FXML private Label phoneLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeNameLabel.setText("Bonjour, " + user.getPrenom() + "!");
            emailLabel.setText(user.getEmail());
            cityLabel.setText(user.getVille() != null ? user.getVille() : "Non renseigne");
            phoneLabel.setText(user.getTelephone() != null ? user.getTelephone() : "Non renseigne");

            String role = user.getRole();
            if ("MEDECIN".equalsIgnoreCase(role)) {
                welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #6B5FD4, #4A4A8A);");
            } else if ("ORGANIZER".equalsIgnoreCase(role)) {
                welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #EF9F27, #D9891B);");
            } else {
                welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #1BBFA8, #159986);");
            }
        }
        
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    }
}
