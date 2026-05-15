package tn.esprit.aura.controllers.patient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import tn.esprit.aura.controllers.SidebarView;
import java.net.URL;
import java.util.ResourceBundle;

public class PatientDashboard implements Initializable {

    @FXML private SidebarView sidebarController;
    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarController.setContentArea(contentArea);
    }
}
