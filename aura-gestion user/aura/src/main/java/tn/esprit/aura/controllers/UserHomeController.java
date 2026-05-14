package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserHomeController {

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
        }
        
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    }
}
