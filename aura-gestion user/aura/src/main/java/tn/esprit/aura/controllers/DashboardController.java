package tn.esprit.aura.controllers;

import tn.esprit.aura.dao.MoodDAO;
import tn.esprit.aura.dao.TaskDAO;
import tn.esprit.aura.entities.Mood;
import tn.esprit.aura.entities.Task;
import tn.esprit.aura.services.VoiceAnalyzer;
import tn.esprit.aura.services.WhisperService;
import tn.esprit.aura.utils.SessionManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.util.List;

/**
 * DashboardController — the AI home/orb screen.
 * Migrated from the Aura AI-core module. Uses SessionManager for user ID.
 */
public class DashboardController {

    @FXML private Label userName;
    @FXML private Label orbText1;
    @FXML private Label orbText2;
    @FXML private Circle orbCircle;
    @FXML private Label timerLabel;
    @FXML private VBox taskList;
    @FXML private Label energyBadge;
    @FXML private Label focusBadge;
    @FXML private Button micButton;
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> taskPriorityBox;
    @FXML private TextField taskDateField;
    @FXML private Label transcriptionLabel;
    @FXML private Label statusLabel;
    @FXML private Label planningSubtitle;

    private Timeline timerTimeline;
    private int secondsLeft = 25 * 60;
    private boolean timerRunning = false;
    private String currentMood = "calme";

    private final TaskDAO taskDAO = new TaskDAO();
    private final MoodDAO moodDAO = new MoodDAO();

    private int getUserId() {
        return SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getId() : 1;
    }

    @FXML
    public void initialize() {
        taskPriorityBox.getItems().addAll("haute", "moyenne", "basse");
        taskPriorityBox.setValue("moyenne");
        if (SessionManager.isLoggedIn())
            userName.setText(SessionManager.getCurrentUser().getNom());
        animateOrb();
        refreshTaskList();
    }

    @FXML
    private void onAddTask() {
        String title    = taskTitleField.getText().trim();
        String priority = taskPriorityBox.getValue();
        String date     = taskDateField.getText().trim();
        if (title.isEmpty()) { statusLabel.setText("Entre un titre de tâche !"); return; }
        if (date.isEmpty()) date = "2026-12-31";
        taskDAO.add(new Task(0, getUserId(), title, priority, "à faire", date));
        taskTitleField.clear(); taskDateField.clear();
        statusLabel.setText("Tâche ajoutée !");
        refreshTaskList();
    }

    private void refreshTaskList() {
        List<Task> tasks = taskDAO.getAll(getUserId());
        displayTasks(tasks);
    }

    private void displayTasks(List<Task> tasks) {
        taskList.getChildren().clear();
        if (tasks.isEmpty()) {
            Label empty = new Label("Aucune tâche — ajoutes-en une !");
            empty.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
            taskList.getChildren().add(empty);
            return;
        }
        for (Task t : tasks) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #1E2329; -fx-background-radius: 10; -fx-padding: 10;");
            Circle dot = new Circle(6);
            if (t.getPriority().equalsIgnoreCase("haute"))       dot.setFill(Color.web("#E85D3A"));
            else if (t.getPriority().equalsIgnoreCase("moyenne")) dot.setFill(Color.web("#EF9F27"));
            else                                                   dot.setFill(Color.web("#1BBFA8"));
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label lTitle    = new Label(t.getTitle());
            lTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
            Label lPriority = new Label(t.getPriority() + " • " + t.getStatus());
            lPriority.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 11;");
            info.getChildren().addAll(lTitle, lPriority);
            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E85D3A; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> { taskDAO.delete(t.getId()); refreshTaskList(); });
            row.getChildren().addAll(dot, info, deleteBtn);
            taskList.getChildren().add(row);
        }
    }

    @FXML
    private void onMicClick() {
        micButton.setDisable(true);
        statusLabel.setText("Enregistrement en cours...");
        new Thread(() -> {
            VoiceAnalyzer analyzer = new VoiceAnalyzer();
            String mood = analyzer.analyzeVoiceMood();
            WhisperService whisper = new WhisperService();
            String text = whisper.transcribe(analyzer.getAudioFilePath());
            moodDAO.add(new Mood(0, getUserId(), mood, null));
            currentMood = mood;
            List<Task> tasks = taskDAO.getAll(getUserId());
            Platform.runLater(() -> {
                updateOrb(mood); updateBadges(mood); displayTasks(tasks);
                if (text != null && !text.isEmpty()) transcriptionLabel.setText("\"" + text + "\"");
                planningSubtitle.setText("Adapté pour humeur : " + mood);
                micButton.setDisable(false);
                statusLabel.setText("Analyse terminée ✓");
            });
        }).start();
    }

    private void updateOrb(String mood) {
        switch (mood) {
            case "stressé"  -> { orbCircle.setFill(Color.web("#E85D3A")); orbText1.setText("Tu sembles stressé."); orbText2.setText("Prends une pause"); }
            case "fatigué"  -> { orbCircle.setFill(Color.web("#4A4A8A")); orbText1.setText("Tu sembles fatigué."); orbText2.setText("Commence doucement 😴"); }
            case "énergisé" -> { orbCircle.setFill(Color.web("#1BBFA8")); orbText1.setText("Tu es plein d'énergie !"); orbText2.setText("Attaque les tâches difficiles ⚡"); }
            case "focalisé" -> { orbCircle.setFill(Color.web("#6B5FD4")); orbText1.setText("Tu es focalisé."); orbText2.setText("Continue comme ça 🎯"); }
            default         -> { orbCircle.setFill(Color.web("#6B5FD4")); orbText1.setText("Tu es calme"); orbText2.setText("& détendu 😌"); }
        }
    }

    private void updateBadges(String mood) {
        energyBadge.setText("ENERGY: " + switch (mood) { case "fatigué" -> "20%"; case "énergisé" -> "85%"; case "stressé" -> "90%"; default -> "50%"; });
        focusBadge.setText("HUMEUR: " + mood.toUpperCase());
    }

    private void animateOrb() {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), orbCircle);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE); pulse.play();
    }

    @FXML
    private void onStartTimer() {
        if (!timerRunning) {
            timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                secondsLeft--;
                timerLabel.setText(String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60));
                if (secondsLeft <= 0) { timerTimeline.stop(); timerRunning = false; statusLabel.setText("Session terminée !"); }
            }));
            timerTimeline.setCycleCount(Timeline.INDEFINITE); timerTimeline.play(); timerRunning = true;
        }
    }

    @FXML
    private void onResetTimer() {
        if (timerTimeline != null) timerTimeline.stop();
        secondsLeft = 25 * 60; timerLabel.setText("25:00"); timerRunning = false;
    }
}
