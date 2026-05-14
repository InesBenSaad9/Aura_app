package View;

import Service.AdaptiveWorkspaceManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private VBox      sidebar;
    @FXML private HBox      rootHBox;
    @FXML private Button    btnTasks, btnAI, btnActivities, btnMedical,
            btnMessages, btnEvents, btnProfile;
    @FXML private Button    btnLightMode;

    private Button  activeButton;
    private boolean lightMode = false;

    @FXML
    public void initialize() {
        AdaptiveWorkspaceManager mgr = AdaptiveWorkspaceManager.getInstance();
        mgr.register(rootHBox, sidebar,
                List.of(btnTasks, btnAI, btnActivities, btnMedical,
                        btnMessages, btnEvents, btnProfile));

        mgr.addListener(() ->
                javafx.application.Platform.runLater(() -> appliquerThemeUI(activeButton))
        );

        showAIAnalysis();
    }

    // ─────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────
    private void loadView(String fxml, Button btn) {
        try {
            activeButton = btn;
            AdaptiveWorkspaceManager.getInstance().setActiveButton(btn);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Node view = loader.load();
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);
            FadeTransition ft = new FadeTransition(Duration.millis(350), view);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            if (!lightMode) appliquerThemeUI(btn);
            else            appliquerLightModeUI();
        } catch (Exception e) {
            System.out.println("Erreur chargement " + fxml + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    // THEME SOMBRE — applique la palette courante
    // ─────────────────────────────────────────────────────────
    private void appliquerThemeUI(Button btnActif) {
        AdaptiveWorkspaceManager.Palette p =
                AdaptiveWorkspaceManager.getInstance().getPaletteActuelle();

        // Fond global — rootHBox couvre tout car contentArea est transparent
        rootHBox.setStyle("-fx-background-color: " + p.background + ";");
        contentArea.setStyle("-fx-background-color: transparent;");

        // Sidebar
        sidebar.setStyle(
                "-fx-background-color: " + p.sidebar + "; " +
                        "-fx-border-width: 0;"
        );

        // Boutons nav
        String inactif =
                "-fx-background-color: transparent; -fx-text-fill: " + p.textSecondary + "; " +
                        "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand;";

        String actif =
                "-fx-background-color: " + p.accentSoft + "; -fx-text-fill: " + p.accent + "; " +
                        "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-border-color: " + p.accent + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 0; " +
                        "-fx-effect: dropshadow(gaussian, " + p.glowColor + ", 8, 0.3, 0, 0);";

        for (Button b : allNavButtons()) {
            if (b == null) continue;
            b.setStyle(b == btnActif ? actif : inactif);
        }

        // Light Mode button stylé selon thème
        if (btnLightMode != null) {
            btnLightMode.setStyle(
                    "-fx-background-color: " + p.accentSoft + "; -fx-text-fill: " + p.accent + "; " +
                            "-fx-font-size: 13; -fx-alignment: CENTER_LEFT; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; " +
                            "-fx-border-color: " + p.accent + "; -fx-border-width: 1; -fx-border-radius: 10;"
            );
            btnLightMode.setText("☀ Light Mode");
        }
    }

    // ─────────────────────────────────────────────────────────
    // LIGHT MODE
    // ─────────────────────────────────────────────────────────
    @FXML
    public void onLightMode() {
        lightMode = !lightMode;
        if (lightMode) appliquerLightModeUI();
        else           appliquerThemeUI(activeButton);
    }

    private void appliquerLightModeUI() {
        rootHBox.setStyle("-fx-background-color: #0A0C0F;");
        contentArea.setStyle("-fx-background-color: transparent;");
        sidebar.setStyle(
                "-fx-background-color: #111418; " +
                        "-fx-border-width: 0;"
        );

        String inactif =
                "-fx-background-color: transparent; -fx-text-fill: #A8B4C0; " +
                        "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand;";

        String actif =
                "-fx-background-color: rgba(27,191,168,0.1); -fx-text-fill: #1BBFA8; " +
                        "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand; " +
                        "-fx-border-color: #1BBFA8; -fx-border-width: 0 0 0 3; -fx-border-radius: 0;";

        for (Button b : allNavButtons()) {
            if (b == null) continue;
            b.setStyle(b == activeButton ? actif : inactif);
        }

        if (btnLightMode != null) {
            btnLightMode.setStyle(
                    "-fx-background-color: rgba(27,191,168,0.1); -fx-text-fill: #1BBFA8; " +
                            "-fx-font-size: 13; -fx-alignment: CENTER_LEFT; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; " +
                            "-fx-border-color: #1BBFA8; -fx-border-width: 1; -fx-border-radius: 10;"
            );
            btnLightMode.setText("Initial Mode");
        }
    }

    private Button[] allNavButtons() {
        return new Button[]{btnTasks, btnAI, btnActivities, btnMedical,
                btnMessages, btnEvents, btnProfile};
    }

    @FXML public void showTasks()      { loadView("tasks.fxml",      btnTasks);      }
    @FXML public void showAIAnalysis() { loadView("analysis.fxml",   btnAI);         }
    @FXML public void showActivities() { loadView("activities.fxml", btnActivities); }
    @FXML public void showMedical()    { loadView("medical.fxml",    btnMedical);    }
    @FXML public void showMessages()   { loadView("messages.fxml",   btnMessages);   }
    @FXML public void showEvents()     { loadView("events.fxml",     btnEvents);     }
    @FXML public void showProfile()    { loadView("profile.fxml",    btnProfile);    }

    @FXML
    public void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            contentArea.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
