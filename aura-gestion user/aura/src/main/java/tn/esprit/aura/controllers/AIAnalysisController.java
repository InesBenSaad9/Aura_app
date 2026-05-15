package tn.esprit.aura.controllers;

import tn.esprit.aura.dao.MoodDAO;
import tn.esprit.aura.entities.Mood;
import tn.esprit.aura.services.AdaptiveWorkspaceManager;
import tn.esprit.aura.services.ElevenLabsService;
import tn.esprit.aura.services.EmotionalScoreEngine;
import tn.esprit.aura.services.EmotionalScoreEngine.EtatEmotionnel;
import tn.esprit.aura.services.EmotionalScoreEngine.ResultatEmotionnel;
import tn.esprit.aura.services.TaskReorganizationEngine;
import tn.esprit.aura.services.TaskReorganizationEngine.ResultatReorganisation;
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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

/**
 * AI Analysis view controller.
 * Migrated from the Aura AI-core module (View.AIAnalysisController).
 * Now reads the current user ID from SessionManager instead of hardcoding it.
 */
public class AIAnalysisController {

    @FXML private Circle      orbCircle;
    @FXML private Circle      pulseRing;
    @FXML private Button      micButton;
    @FXML private Label       orbEmoji;
    @FXML private Label       orbMoodText;
    @FXML private Label       orbSubText;
    @FXML private Label       statusLabel;
    @FXML private Label       moodLabel;
    @FXML private Label       energyLabel;
    @FXML private Label       pitchLabel;
    @FXML private Label       transcriptionLabel;
    @FXML private ProgressBar energyBar;
    @FXML private HBox        resultsBox;
    @FXML private VBox        transcriptionBox;
    @FXML private HBox        chartBox;
    @FXML private HBox        daysBox;
    @FXML private VBox        historyList;

    @FXML private Label emotionalScoreLabel;
    @FXML private Label recommandationLabel;
    @FXML private VBox  emotionalPanel;

    @FXML private TextField  chatInput;
    @FXML private VBox       chatMessagesBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button     chatSendBtn;
    @FXML private VBox       chatPanel;
    @FXML private VBox       reorganisationPanel;
    @FXML private Label      reorganisationMessage;
    @FXML private Label      reorganisationConseil;
    @FXML private VBox       tachesRecommandeesBox;

    private final MoodDAO                 moodDAO        = new MoodDAO();
    private final EmotionalScoreEngine    emotionalEngine = new EmotionalScoreEngine();
    private final TaskReorganizationEngine taskEngine     = new TaskReorganizationEngine();
    private final ElevenLabsService       elevenLabsService = new ElevenLabsService();

    private ScaleTransition pulseAnim;
    private boolean         isRecording       = false;
    private String          contextEmotionnel = "L'utilisateur n'a pas encore fait d'analyse vocale.";

    /** Returns the current user ID from session (or 1 as fallback). */
    private int getCurrentUserId() {
        if (SessionManager.isLoggedIn()) return SessionManager.getCurrentUser().getId();
        return 1;
    }

    @FXML
    public void initialize() {
        animateOrb();
        loadMoodHistory();
        appliquerCouleursMicBtn(AdaptiveWorkspaceManager.getInstance().getAccentColor());
    }

    @FXML
    public void onMicClick() {
        if (isRecording) return;
        isRecording = true;
        micButton.setDisable(true);
        statusLabel.setText("Enregistrement en cours...");
        startPulseRing();

        new Thread(() -> {
            try {
                int userId = getCurrentUserId();

                VoiceAnalyzer analyzer = new VoiceAnalyzer();
                String detectedMood   = analyzer.analyzeVoiceMood();
                double energy         = analyzer.getLastEnergy();
                double pitch          = analyzer.getLastPitch();

                WhisperService whisper  = new WhisperService();
                String transcription    = whisper.transcribe(analyzer.getAudioFilePath());

                ResultatEmotionnel resultat = emotionalEngine.analyser(userId, detectedMood, energy);

                Mood mood = new Mood(0, userId, detectedMood, null);
                moodDAO.add(mood);

                ResultatReorganisation reo = taskEngine.reorganiser(userId, detectedMood);

                Platform.runLater(() -> {
                    stopPulseRing();
                    updateResults(detectedMood, energy, pitch);
                    updateOrbEmotionnel(detectedMood, resultat);
                    afficherScoreEmotionnel(resultat);
                    afficherReorganisation(reo);

                    if (transcription != null && !transcription.isBlank()) {
                        transcriptionBox.setVisible(true);
                        transcriptionBox.setManaged(true);
                        transcriptionLabel.setText(transcription);
                    }

                    statusLabel.setText("Analyse terminée ✓");
                    micButton.setDisable(false);
                    appliquerCouleursMicBtn(AdaptiveWorkspaceManager.getInstance().getAccentColor());
                    loadMoodHistory();
                });

                AdaptiveWorkspaceManager.getInstance().appliquerDepuisHumeur(detectedMood);

                boolean success = elevenLabsService.envoyerEtRepondre(transcription, detectedMood);
                Platform.runLater(() -> {
                    if (success) ajouterMessageAura("🔊 AURA te répond vocalement...");
                    else         ajouterMessageAura(getFallbackMessage(detectedMood));
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur : " + e.getMessage());
                    micButton.setDisable(false);
                    stopPulseRing();
                });
            } finally {
                isRecording = false;
            }
        }, "AURA-Recording-Thread").start();
    }

    private void updateOrbEmotionnel(String mood, ResultatEmotionnel resultat) {
        String couleur = emotionalEngine.getCouleurOrbe(resultat.etat);
        orbCircle.setFill(Color.web(couleur));
        orbEmoji.setText(switch (resultat.etat) {
            case STRESSE  -> "😰";
            case FATIGUE  -> "😴";
            case CALME    -> "😊";
            case ENERGISE -> "⚡";
        });
        orbMoodText.setText(resultat.messageUI);
        orbSubText.setText("Humeur détectée : " + mood);
    }

    private void afficherScoreEmotionnel(ResultatEmotionnel resultat) {
        if (emotionalScoreLabel != null) {
            emotionalScoreLabel.setText("Score émotionnel : " + resultat.score + "/100");
            String couleurScore = switch (resultat.etat) {
                case STRESSE  -> "#E85D3A";
                case FATIGUE  -> "#6B5FD4";
                default       -> "#1BBFA8";
            };
            emotionalScoreLabel.setStyle("-fx-text-fill: " + couleurScore + "; -fx-font-size: 22; -fx-font-weight: bold;");
        }
        if (recommandationLabel != null) {
            recommandationLabel.setText(resultat.recommandation);
            recommandationLabel.setVisible(true);
            recommandationLabel.setManaged(true);
        }
        if (emotionalPanel != null) {
            emotionalPanel.setVisible(true);
            emotionalPanel.setManaged(true);
            AdaptiveWorkspaceManager.getInstance().appliquerTheme(resultat.theme);
            contextEmotionnel = "Score émotionnel : " + resultat.score + "/100. État : "
                    + resultat.etat + ". " + resultat.recommandation;
            if (chatMessagesBox.getChildren().isEmpty()) {
                ajouterMessageAura("Bonjour ! Je suis AURA " + getEmojiEtat(resultat.etat)
                        + " J'ai analysé ton état — " + resultat.messageUI + ". Tu veux qu'on en parle ?");
            }
            if (chatPanel != null) { chatPanel.setVisible(true); chatPanel.setManaged(true); }
        }
    }

    private void updateResults(String mood, double energy, double pitch) {
        resultsBox.setVisible(true);
        resultsBox.setManaged(true);
        moodLabel.setText(mood.toUpperCase());
        double energyPct = Math.min(energy / 5000.0, 1.0);
        energyBar.setProgress(energyPct);
        energyLabel.setText(String.format("%.0f%%", energyPct * 100));
        pitchLabel.setText(String.format("%.0f", pitch));
    }

    private void loadMoodHistory() {
        List<Mood> history = moodDAO.getAll(getCurrentUserId());
        chartBox.getChildren().clear();
        daysBox.getChildren().clear();
        historyList.getChildren().clear();

        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        int size = Math.min(history.size(), 7);

        for (int i = 0; i < 7; i++) {
            String moodOfDay = i < size ? history.get(size - 1 - i).getLabel() : "calme";
            VBox barContainer = new VBox();
            barContainer.setAlignment(Pos.BOTTOM_CENTER);
            barContainer.setPrefHeight(160);
            HBox.setHgrow(barContainer, Priority.ALWAYS);
            Rectangle bar = new Rectangle(40, getMoodHeight(moodOfDay));
            bar.setFill(Color.web(getMoodColor(moodOfDay)));
            bar.setArcWidth(8); bar.setArcHeight(8);
            barContainer.getChildren().add(bar);
            chartBox.getChildren().add(barContainer);

            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
            dayLabel.setPrefWidth(52);
            dayLabel.setAlignment(Pos.CENTER);
            daysBox.getChildren().add(dayLabel);
        }

        for (Mood m : history) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            String cardBg = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;
            row.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 10; -fx-padding: 12;");
            Circle dot = new Circle(6);
            dot.setFill(Color.web(getMoodColor(m.getLabel())));
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label moodLbl = new Label(m.getLabel().toUpperCase());
            moodLbl.setStyle("-fx-text-fill: " + AdaptiveWorkspaceManager.getInstance().getTextPrimaryColor()
                    + "; -fx-font-size: 13; -fx-font-weight: bold;");
            Label dateLbl = new Label(m.getDetectedAt() != null
                    ? m.getDetectedAt().substring(0, Math.min(16, m.getDetectedAt().length())) : "—");
            dateLbl.setStyle("-fx-text-fill: " + AdaptiveWorkspaceManager.getInstance().getTextSecondaryColor()
                    + "; -fx-font-size: 11;");
            info.getChildren().addAll(moodLbl, dateLbl);
            row.getChildren().addAll(dot, info);
            historyList.getChildren().add(0, row);
        }
    }

    private void animateOrb() {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), orbCircle);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void startPulseRing() {
        pulseAnim = new ScaleTransition(Duration.seconds(1), pulseRing);
        pulseAnim.setFromX(1.0); pulseAnim.setToX(1.8);
        pulseAnim.setFromY(1.0); pulseAnim.setToY(1.8);
        pulseAnim.setAutoReverse(true);
        pulseAnim.setCycleCount(Animation.INDEFINITE);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pulseRing);
        fade.setFromValue(0.5); fade.setToValue(0.0);
        fade.setAutoReverse(true); fade.setCycleCount(Animation.INDEFINITE);
        pulseAnim.play(); fade.play();
    }

    private void stopPulseRing() {
        if (pulseAnim != null) pulseAnim.stop();
        pulseRing.setScaleX(1.0); pulseRing.setScaleY(1.0); pulseRing.setOpacity(0.3);
    }

    @FXML
    private void onChatSend() {
        String texte = chatInput.getText().trim();
        if (texte.isEmpty()) return;
        chatInput.clear();
        chatSendBtn.setDisable(true);
        ajouterMessageUser(texte);
        Label typing = ajouterMessageAura("AURA réfléchit...");
        new Thread(() -> {
            String reponse = genererReponseAura(texte);
            Platform.runLater(() -> {
                chatMessagesBox.getChildren().remove(typing.getParent());
                ajouterMessageAura(reponse);
                chatSendBtn.setDisable(false);
                chatScrollPane.setVvalue(1.0);
            });
        }).start();
    }

    private void ajouterMessageUser(String texte) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);
        Label bubble = new Label(texte);
        bubble.setWrapText(true);
        bubble.setMaxWidth(280);
        String accent = AdaptiveWorkspaceManager.getInstance().getAccentColor();
        bubble.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: #0A0C0F; "
                + "-fx-background-radius: 16 16 4 16; -fx-padding: 10 14; -fx-font-size: 13;");
        row.getChildren().add(bubble);
        chatMessagesBox.getChildren().add(row);
        chatScrollPane.setVvalue(1.0);
    }

    private Label ajouterMessageAura(String texte) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(getEmojiEtat(mapThemeToEtat(AdaptiveWorkspaceManager.getInstance().getThemeActuel())));
        avatar.setStyle("-fx-font-size: 16;");
        String cardBg = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;
        String border = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().borderColor;
        Label bubble = new Label(texte);
        bubble.setWrapText(true); bubble.setMaxWidth(260);
        bubble.setStyle("-fx-background-color: " + cardBg + "; -fx-text-fill: rgba(255,255,255,0.9); "
                + "-fx-background-radius: 16 16 16 4; -fx-padding: 10 14; -fx-font-size: 13; "
                + "-fx-border-color: " + border + "; -fx-border-radius: 16 16 16 4; -fx-border-width: 1;");
        row.getChildren().addAll(avatar, bubble);
        chatMessagesBox.getChildren().add(row);
        chatScrollPane.setVvalue(1.0);
        return bubble;
    }

    private String genererReponseAura(String messageUser) {
        String msg  = messageUser.toLowerCase().trim();
        String etat = contextEmotionnel.toLowerCase();
        if (msg.matches(".*(bonjour|salut|hello|coucou|hey).*")) {
            if (etat.contains("stresse")) return "Bonjour 💜 Je vois que tu traverses un moment difficile. Respire, on y va doucement.";
            if (etat.contains("fatigue")) return "Bonjour 🌙 Tu sembles fatigué(e). Commence petit — une tâche simple, puis une pause.";
            if (etat.contains("energise")) return "Bonjour ! ⚡ Tu es en pleine forme — c'est le moment d'attaquer tes objectifs !";
            return "Bonjour ! 🌿 Comment puis-je t'aider à organiser ta journée ?";
        }
        if (msg.matches(".*(stress|anxieu|peur|angoiss|debord).*"))
            return "Je comprends 💜 Respire. AURA a réorganisé tes tâches. Une seule chose à la fois.";
        if (msg.matches(".*(fatigu|epuis|dormir|sommeil).*"))
            return "Tu mériterais du repos 🌙 Commence par 15 minutes sur la tâche la plus simple. Ton énergie reviendra.";
        if (msg.matches(".*(revision|etudi|travaill|commenc).*")) {
            if (etat.contains("energise")) return "C'est le moment parfait ! 🚀 Attaque le sujet difficile pendant que tu es dans la zone.";
            return "Bonne idée ! 📚 Commence par tes priorités et fais une pause toutes les 25 minutes.";
        }
        if (msg.matches(".*(tache|task|priorite|organis|planning).*"))
            return "Tes tâches prioritaires sont prêtes 📋 Commence par celles avec les deadlines les plus proches.";
        if (msg.matches(".*(merci|thanks|super|top).*"))
            return "Avec plaisir ! 😊 Je suis là quand tu as besoin. Continue comme ça !";
        if (etat.contains("stresse")) return "Je t'écoute 💜 Tu n'es pas seul(e) — AURA adapte ton planning pour que tu te sentes moins débordé(e).";
        if (etat.contains("fatigue")) return "Prends soin de toi 🌙 Allons-y doucement aujourd'hui.";
        if (etat.contains("energise")) return "Tu es en pleine forme ! ⚡ Dis-moi comment maximiser cette belle énergie.";
        return "Je t'écoute 🌿 Comment puis-je t'aider à mieux organiser ta journée ?";
    }

    private void afficherReorganisation(ResultatReorganisation reo) {
        if (reorganisationPanel == null) return;
        reorganisationPanel.setVisible(true);
        reorganisationPanel.setManaged(true);
        if (reorganisationMessage != null) reorganisationMessage.setText(reo.messageUtilisateur);
        if (reorganisationConseil  != null) reorganisationConseil.setText(reo.conseil);
        if (tachesRecommandeesBox != null) {
            tachesRecommandeesBox.getChildren().clear();
            String cardBg = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;
            for (tn.esprit.aura.entities.Task t : reo.tachesRecommandees) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 10; -fx-padding: 10 14;");
                Circle dot = new Circle(5);
                dot.setFill(Color.web(couleurPriorite(t.getPriority())));
                Label titre = new Label(t.getTitle());
                titre.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
                HBox.setHgrow(titre, Priority.ALWAYS);
                Label badge = new Label(t.getPriority());
                badge.setStyle("-fx-background-color: " + couleurPriorite(t.getPriority())
                        + "33; -fx-text-fill: " + couleurPriorite(t.getPriority())
                        + "; -fx-font-size: 10; -fx-background-radius: 6; -fx-padding: 2 8;");
                row.getChildren().addAll(dot, titre, badge);
                tachesRecommandeesBox.getChildren().add(row);
            }
            if (!reo.tachesReportees.isEmpty()) {
                Label reportees = new Label("⏸ " + reo.tachesReportees.size() + " tâche(s) haute priorité reportées pour plus tard");
                reportees.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 11; -fx-padding: 4 0 0 0;");
                tachesRecommandeesBox.getChildren().add(reportees);
            }
        }
    }

    private String couleurPriorite(String p) {
        if (p == null) return "#A8B4C0";
        return switch (p.toLowerCase()) {
            case "haute"   -> "#E85D3A";
            case "moyenne" -> "#EF9F27";
            case "basse"   -> "#1BBFA8";
            default        -> "#A8B4C0";
        };
    }

    private String getMoodColor(String mood) {
        if (mood == null) return "#1BBFA8";
        return switch (mood.toLowerCase().trim()) {
            case "stressé",  "stresse" -> "#E85D3A";
            case "fatigué",  "fatigue" -> "#6B5FD4";
            default                    -> "#1BBFA8";
        };
    }

    private double getMoodHeight(String mood) {
        if (mood == null) return 90;
        return switch (mood.toLowerCase().trim()) {
            case "énergisé", "energise" -> 140;
            case "focalisé"              -> 120;
            case "calme"                 -> 100;
            case "stressé", "stresse"   -> 70;
            case "fatigué", "fatigue"   -> 55;
            default                      -> 90;
        };
    }

    private String getEmojiEtat(EtatEmotionnel etat) {
        if (etat == null) return "🌟";
        return switch (etat) {
            case STRESSE  -> "💜";
            case FATIGUE  -> "🌙";
            case CALME    -> "🌿";
            case ENERGISE -> "⚡";
        };
    }

    private EtatEmotionnel mapThemeToEtat(AdaptiveWorkspaceManager.Theme theme) {
        if (theme == null) return EtatEmotionnel.CALME;
        return switch (theme) {
            case INITIAL, CALME -> EtatEmotionnel.CALME;
            case STRESSE        -> EtatEmotionnel.STRESSE;
            case FATIGUE        -> EtatEmotionnel.FATIGUE;
            case ENERGISE       -> EtatEmotionnel.ENERGISE;
        };
    }

    private void appliquerCouleursMicBtn(String accent) {
        micButton.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: #0A0C0F; "
                + "-fx-background-radius: 40; -fx-padding: 20 24; -fx-font-size: 14; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, " + accent + ", 14, 0.4, 0, 2);");
    }

    private String getFallbackMessage(String moodLabel) {
        return switch (moodLabel.toLowerCase().trim()) {
            case "stressé", "stresse"   -> "Hey, respire. J'ai allégé ta liste pour toi. 💙";
            case "fatigué", "fatigue"   -> "Tu as besoin de récupérer. Je garde l'essentiel pour aujourd'hui. 🌙";
            case "calme"                -> "Parfait état pour avancer. Tes tâches sont prêtes. ✨";
            case "énergisé", "energise" -> "Tu es en feu ! Tes priorités hautes sont devant. 🚀";
            default                     -> "Je suis là pour t'aider. 💚";
        };
    }
}
