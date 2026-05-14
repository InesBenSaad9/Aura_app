package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.aura.services.UserService;

public class AdminHomeController {

    @FXML private Label totalUsersCount;
    @FXML private Label adminsCount;
    @FXML private Label activeCount;
    @FXML private Label newTodayCount;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            totalUsersCount.setText(String.valueOf(userService.countAll()));
            adminsCount.setText(String.valueOf(userService.countByRole("ADMIN")));
            activeCount.setText(String.valueOf(userService.countActive()));
            newTodayCount.setText(String.valueOf(userService.countNewToday()));
        } catch (Exception e) {
            totalUsersCount.setText("—");
            adminsCount.setText("—");
            activeCount.setText("—");
            newTodayCount.setText("—");
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }
}
