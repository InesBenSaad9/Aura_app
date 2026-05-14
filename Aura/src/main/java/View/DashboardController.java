package View;

import Controller.PlanningController;
import DAO.MoodDAO;
import DAO.TaskDAO;
import Model.Mood;
import Model.Task;
import Service.VoiceAnalyzer;
import Service.WhisperService;
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

    private TaskDAO taskDAO = new TaskDAO();
    private MoodDAO moodDAO = new MoodDAO();
    private PlanningController planningController = new PlanningController();

    @FXML
    public void initialize() {
        // Remplis le ComboBox
        taskPriorityBox.getItems().addAll("haute", "moyenne", "basse");
        taskPriorityBox.setValue("moyenne");

        animateOrb();
        refreshTaskList();
    }

    // ═══════════════════════════════
    // GESTION DES TÂCHES
    // ═══════════════════════════════

    @FXML
    private void onAddTask() {
        String title = taskTitleField.getText().trim();
        String priority = taskPriorityBox.getValue();
        String date = taskDateField.getText().trim();

        if (title.isEmpty()) {
            statusLabel.setText("Entre un titre de tâche !");
            return;
        }
        if (date.isEmpty()) date = "2025-12-31";

        Task task = new Task(0, 1, title, priority, "à faire", date);
        taskDAO.add(task);

        taskTitleField.clear();
        taskDateField.clear();
        statusLabel.setText("Tâche ajoutée !");

        refreshTaskList();
    }

    private void refreshTaskList() {
        List<Task> tasks = planningController.generatePlanning(1, currentMood);
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

            // Couleur priorité
            Circle dot = new Circle(6);
            if (t.getPriority().equalsIgnoreCase("haute")) dot.setFill(Color.web("#E85D3A"));
            else if (t.getPriority().equalsIgnoreCase("moyenne")) dot.setFill(Color.web("#EF9F27"));
            else dot.setFill(Color.web("#1BBFA8"));

            // Infos tâche
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label title = new Label(t.getTitle());
            title.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
            Label priority = new Label(t.getPriority() + " • " + t.getStatus());
            priority.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 11;");
            info.getChildren().addAll(title, priority);

            // Bouton Modifier
            Button editBtn = new Button("✏️");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-cursor: hand; -fx-font-size: 14;");
            editBtn.setOnAction(e -> onEditTask(t));

            // Bouton Supprimer
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E85D3A; -fx-cursor: hand; -fx-font-size: 14;");
            deleteBtn.setOnAction(e -> onDeleteTask(t));

            row.getChildren().addAll(dot, info, editBtn, deleteBtn);
            taskList.getChildren().add(row);
        }
    }

    private void onEditTask(Task t) {
        // Remplis les champs avec les valeurs actuelles
        taskTitleField.setText(t.getTitle());
        taskPriorityBox.setValue(t.getPriority());
        taskDateField.setText(t.getScheduledAt());

        // Supprime l'ancienne et l'utilisateur va réajouter
        taskDAO.delete(t.getId());
        refreshTaskList();
        statusLabel.setText("✏️ Modifie et réajoute la tâche !");
    }

    private void onDeleteTask(Task t) {
        taskDAO.delete(t.getId());
        refreshTaskList();
        statusLabel.setText("🗑️ Tâche supprimée !");
    }

    // ═══════════════════════════════
    // ANALYSE VOIX
    // ═══════════════════════════════

    @FXML
    private void onMicClick() {
        micButton.setText("🔴");
        micButton.setDisable(true);
        statusLabel.setText("🎤 Enregistrement en cours...");

        new Thread(() -> {
            // Analyse voix
            VoiceAnalyzer analyzer = new VoiceAnalyzer();
            String mood = analyzer.analyzeVoiceMood();
            double energy = analyzer.calculateEnergy(new byte[0]);

            // Transcription
            WhisperService whisper = new WhisperService();
            String text = whisper.transcribe(analyzer.getAudioFilePath());

            // Sauvegarde humeur
            moodDAO.add(new Mood(0, 1, mood, ""));

            // Planning adapté
            currentMood = mood;
            List<Task> planning = planningController.generatePlanning(1, mood);

            // Mise à jour interface
            Platform.runLater(() -> {
                updateOrb(mood);
                updateBadges(mood);
                displayTasks(planning);

                if (text != null && !text.isEmpty()) {
                    transcriptionLabel.setText("\"" + text + "\"");
                }

                planningSubtitle.setText("Adapté pour humeur : " + mood);
                micButton.setText("🎤");
                micButton.setDisable(false);
                statusLabel.setText("✅ Analyse terminée !");
            });
        }).start();
    }

    private void updateOrb(String mood) {
        switch (mood) {
            case "stressé" -> {
                orbCircle.setFill(Color.web("#E85D3A"));
                orbText1.setText("Tu sembles stressé.");
                orbText2.setText("Prends une pause");
            }
            case "fatigué" -> {
                orbCircle.setFill(Color.web("#4A4A8A"));
                orbText1.setText("Tu sembles fatigué.");
                orbText2.setText("Commence doucement 😴");
            }
            case "énergisé" -> {
                orbCircle.setFill(Color.web("#1BBFA8"));
                orbText1.setText("Tu es plein d'énergie !");
                orbText2.setText("Attaque les tâches difficiles ⚡");
            }
            case "focalisé" -> {
                orbCircle.setFill(Color.web("#6B5FD4"));
                orbText1.setText("Tu es focalisé.");
                orbText2.setText("Continue comme ça 🎯");
            }
            default -> {
                orbCircle.setFill(Color.web("#6B5FD4"));
                orbText1.setText("Tu es calme");
                orbText2.setText("& détendu 😌");
            }
        }
    }

    private void updateBadges(String mood) {
        String[] energyMap = {"fatigué", "calme", "focalisé", "énergisé", "stressé"};
        String[] energyLevel = {"Low", "Normal", "Medium", "High", "Very High"};
        String[] energyPct = {"20%", "50%", "65%", "85%", "90%"};

        int idx = 1;
        for (int i = 0; i < energyMap.length; i++) {
            if (energyMap[i].equals(mood)) { idx = i; break; }
        }

        energyBadge.setText("ENERGY: " + energyPct[idx] + " / " + energyLevel[idx]);
        focusBadge.setText("HUMEUR: " + mood.toUpperCase());
    }

    // ═══════════════════════════════
    // ORB ANIMATION
    // ═══════════════════════════════

    private void animateOrb() {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), orbCircle);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    // ═══════════════════════════════
    // TIMER
    // ═══════════════════════════════

    @FXML
    private void onStartTimer() {
        if (!timerRunning) {
            timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                secondsLeft--;
                int min = secondsLeft / 60;
                int sec = secondsLeft % 60;
                timerLabel.setText(String.format("%02d:%02d", min, sec));
                if (secondsLeft <= 0) {
                    timerTimeline.stop();
                    timerRunning = false;
                    statusLabel.setText("⏱ Session terminée !");
                }
            }));
            timerTimeline.setCycleCount(Timeline.INDEFINITE);
            timerTimeline.play();
            timerRunning = true;
        }
    }

    @FXML
    private void onResetTimer() {
        if (timerTimeline != null) timerTimeline.stop();
        secondsLeft = 25 * 60;
        timerLabel.setText("25:00");
        timerRunning = false;
    }
}