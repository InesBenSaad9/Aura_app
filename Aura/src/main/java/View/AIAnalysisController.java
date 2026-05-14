package View;

import DAO.MoodDAO;
import Model.Mood;
import Service.AdaptiveWorkspaceManager;
import Service.EmotionalScoreEngine;
import Service.EmotionalScoreEngine.ResultatEmotionnel;
import Service.EmotionalScoreEngine.EtatEmotionnel;
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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import Service.TaskReorganizationEngine;
import Service.TaskReorganizationEngine.ResultatReorganisation;
import java.util.List;
import Service.ElevenLabsService;
public class AIAnalysisController {

    // ── FXML existants ────────────────────────────────────────
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

    // ── FXML panel emotionnel ─────────────────────────────────
    @FXML private Label emotionalScoreLabel;
    @FXML private Label recommandationLabel;
    @FXML private VBox  emotionalPanel;

    // ── FXML chat ─────────────────────────────────────────────
    @FXML private TextField  chatInput;
    @FXML private VBox       chatMessagesBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button     chatSendBtn;
    @FXML private VBox  reorganisationPanel;   // panel entier
    @FXML private Label reorganisationMessage; // "AURA a allégé ta liste"
    @FXML private Label reorganisationConseil; // "💜 Mode Zen..."
    @FXML private VBox  tachesRecommandeesBox; // liste des tâches suggérées
    // ── Services ──────────────────────────────────────────────
    private final MoodDAO              moodDAO        = new MoodDAO();
    private final EmotionalScoreEngine emotionalEngine = new EmotionalScoreEngine();
    private final TaskReorganizationEngine taskEngine = new TaskReorganizationEngine();
    private final ElevenLabsService elevenLabsService = new ElevenLabsService();

    // ── Etat ──────────────────────────────────────────────────
    private ScaleTransition pulseAnim;
    private boolean         isRecording      = false;
    private int             currentUserId    = 1;
    private String          contextEmotionnel = "L'utilisateur n'a pas encore fait d'analyse vocale.";
    @FXML private VBox chatPanel;
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        animateOrb();
        loadMoodHistory();
        appliquerCouleursMicBtn(AdaptiveWorkspaceManager.getInstance().getAccentColor());
    }

    // =========================================================
    // BOUTON MICRO
    // =========================================================
    // =========================================================
    // BOUTON MICRO — remplace ton onMicClick() actuel
    // =========================================================
    // =========================================================
    // BOUTON MICRO — remplace ton onMicClick() actuel
    // =========================================================
    // =========================================================
    // BOUTON MICRO — version finale avec GPT + TTS
    // =========================================================
    @FXML
    public void onMicClick() {
        if (isRecording) return;
        isRecording = true;

        micButton.setDisable(true);
        statusLabel.setText("Enregistrement en cours...");
        startPulseRing();

        new Thread(() -> {
            try {
                // ── Étape 1 : Enregistrement vocal ────────────────────────
                VoiceAnalyzer analyzer = new VoiceAnalyzer();
                String detectedMood   = analyzer.analyzeVoiceMood();
                double energy         = analyzer.getLastEnergy();
                double pitch          = analyzer.getLastPitch();

                // ── Étape 2 : Transcription Whisper ───────────────────────
                WhisperService whisper = new WhisperService();
                String transcription   = whisper.transcribe(analyzer.getAudioFilePath());
                System.out.println("[AURA] Transcription : " + transcription);

                // ── Étape 3 : Score émotionnel ────────────────────────────
                ResultatEmotionnel resultat = emotionalEngine.analyser(
                        currentUserId, detectedMood, energy
                );

                // ── Étape 4 : Sauvegarde en base ──────────────────────────
                Mood mood = new Mood(0, currentUserId, detectedMood, null);
                moodDAO.add(mood);

                // ── Étape 5 : Réorganisation des tâches ───────────────────
                ResultatReorganisation reo = taskEngine.reorganiser(currentUserId, detectedMood);

                // ── Étape 6 : Mise à jour UI ──────────────────────────────
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
                    appliquerCouleursMicBtn(
                            AdaptiveWorkspaceManager.getInstance().getAccentColor()
                    );
                    loadMoodHistory();
                });

                // ── Étape 7 : Thème adaptatif ─────────────────────────────
                AdaptiveWorkspaceManager.getInstance().appliquerDepuisHumeur(detectedMood);

                // ── Étape 8 : GPT génère réponse → ElevenLabs Bella parle ─
                // On passe la transcription ET le mood pour une réponse intelligente
                boolean success = elevenLabsService.envoyerEtRepondre(transcription, detectedMood);

                Platform.runLater(() -> {
                    if (success) {
                        ajouterMessageAura("🔊 AURA te répond vocalement...");
                    } else {
                        ajouterMessageAura(getFallbackMessage(detectedMood));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur : " + e.getMessage());
                    micButton.setDisable(false);
                    stopPulseRing();
                });
                e.printStackTrace();
            } finally {
                isRecording = false;
            }
        }, "AURA-Recording-Thread").start();
    }
    // =========================================================
    // ORBE — mise a jour selon les 4 etats
    // =========================================================
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
        orbSubText.setText("Humeur detectee : " + mood);
    }

    // =========================================================
    // PANEL SCORE EMOTIONNEL
    // =========================================================
    private void afficherScoreEmotionnel(ResultatEmotionnel resultat) {

        if (emotionalScoreLabel != null) {
            emotionalScoreLabel.setText("Score emotionnel : " + resultat.score + "/100");
            String couleurScore = switch (resultat.etat) {
                case STRESSE  -> "#E85D3A";
                case FATIGUE  -> "#6B5FD4";
                case CALME    -> "#1BBFA8";
                case ENERGISE -> "#1BBFA8";
            };
            emotionalScoreLabel.setStyle(
                    "-fx-text-fill: " + couleurScore + "; " +
                            "-fx-font-size: 22; -fx-font-weight: bold;"
            );
        }

        if (recommandationLabel != null) {
            recommandationLabel.setText(resultat.recommandation);
            recommandationLabel.setVisible(true);
            recommandationLabel.setManaged(true);
        }

        if (emotionalPanel != null) {
            emotionalPanel.setVisible(true);
            emotionalPanel.setManaged(true);

            // Applique le theme adaptatif
            AdaptiveWorkspaceManager.getInstance().appliquerTheme(resultat.theme);

            contextEmotionnel = "Score emotionnel : " + resultat.score + "/100. Etat : "
                    + resultat.etat + ". " + resultat.recommandation;

            if (chatMessagesBox.getChildren().isEmpty()) {
                ajouterMessageAura("Bonjour ! Je suis AURA " + getEmojiEtat(resultat.etat)
                        + " J'ai analyse ton etat — " + resultat.messageUI
                        + ". Tu veux qu'on en parle ?");
            }
            if (chatPanel != null) {
                chatPanel.setVisible(true);
                chatPanel.setManaged(true);
            }
        }

        System.out.println("================================================");
        System.out.println("AURA — Score emotionnel : " + resultat.score + "/100");
        System.out.println("Etat    : " + resultat.etat);
        System.out.println("Theme   : " + resultat.theme);
        System.out.println("Conseil : " + resultat.recommandation);
        System.out.println("================================================");
    }

    // =========================================================
    // RESULTATS VOCAUX
    // =========================================================
    private void updateResults(String mood, double energy, double pitch) {
        resultsBox.setVisible(true);
        resultsBox.setManaged(true);

        moodLabel.setText(mood.toUpperCase());

        double energyPct = Math.min(energy / 5000.0, 1.0);
        energyBar.setProgress(energyPct);
        energyLabel.setText(String.format("%.0f%%", energyPct * 100));
        pitchLabel.setText(String.format("%.0f", pitch));
    }

    // =========================================================
    // HISTORIQUE
    // =========================================================
    private void loadMoodHistory() {
        List<Mood> history = moodDAO.getAll(currentUserId);

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
            bar.setArcWidth(8);
            bar.setArcHeight(8);
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
            // Utilise la couleur de card du theme actuel
            String cardBg = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;
            row.setStyle("-fx-background-color: " + cardBg + "; " +
                    "-fx-background-radius: 10; -fx-padding: 12;");

            Circle dot = new Circle(6);
            dot.setFill(Color.web(getMoodColor(m.getLabel())));

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label moodLbl = new Label(m.getLabel().toUpperCase());
            moodLbl.setStyle("-fx-text-fill: "
                    + AdaptiveWorkspaceManager.getInstance().getTextPrimaryColor()
                    + "; -fx-font-size: 13; -fx-font-weight: bold;");

            Label dateLbl = new Label(m.getDetectedAt() != null
                    ? m.getDetectedAt().substring(0, Math.min(16, m.getDetectedAt().length()))
                    : "—");
            dateLbl.setStyle("-fx-text-fill: "
                    + AdaptiveWorkspaceManager.getInstance().getTextSecondaryColor()
                    + "; -fx-font-size: 11;");

            info.getChildren().addAll(moodLbl, dateLbl);
            row.getChildren().addAll(dot, info);
            historyList.getChildren().add(0, row);
        }
    }

    // =========================================================
    // ANIMATIONS
    // =========================================================
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
        fade.setFromValue(0.5);
        fade.setToValue(0.0);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);

        pulseAnim.play();
        fade.play();
    }

    private void stopPulseRing() {
        if (pulseAnim != null) pulseAnim.stop();
        pulseRing.setScaleX(1.0);
        pulseRing.setScaleY(1.0);
        pulseRing.setOpacity(0.3);
    }

    // =========================================================
    // CHAT
    // =========================================================
    @FXML
    private void onChatSend() {
        String texte = chatInput.getText().trim();
        if (texte.isEmpty()) return;

        chatInput.clear();
        chatSendBtn.setDisable(true);
        ajouterMessageUser(texte);

        Label typing = ajouterMessageAura("AURA reflechit...");

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
        // Couleur de bulle = accent du theme actuel
        String accent = AdaptiveWorkspaceManager.getInstance().getAccentColor();
        bubble.setStyle(
                "-fx-background-color: " + accent + "; " +
                        "-fx-text-fill: #0A0C0F; " +
                        "-fx-background-radius: 16 16 4 16; " +
                        "-fx-padding: 10 14; -fx-font-size: 13;"
        );
        row.getChildren().add(bubble);
        chatMessagesBox.getChildren().add(row);
        chatScrollPane.setVvalue(1.0);
    }

    private Label ajouterMessageAura(String texte) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(getEmojiEtat(
                AdaptiveWorkspaceManager.getInstance().getThemeActuel() == null
                        ? null
                        : mapThemeToEtat(AdaptiveWorkspaceManager.getInstance().getThemeActuel())
        ));
        avatar.setStyle("-fx-font-size: 16;");

        String cardBg  = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;
        String border  = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().borderColor;

        Label bubble = new Label(texte);
        bubble.setWrapText(true);
        bubble.setMaxWidth(260);
        bubble.setStyle(
                "-fx-background-color: " + cardBg + "; " +
                        "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-background-radius: 16 16 16 4; " +
                        "-fx-padding: 10 14; -fx-font-size: 13; " +
                        "-fx-border-color: " + border + "; " +
                        "-fx-border-radius: 16 16 16 4; -fx-border-width: 1;"
        );

        row.getChildren().addAll(avatar, bubble);
        chatMessagesBox.getChildren().add(row);
        chatScrollPane.setVvalue(1.0);
        return bubble;
    }

    // =========================================================
    // REPONSES CHAT — alignees sur les 4 etats
    // =========================================================
    private String genererReponseAura(String messageUser) {
        String msg  = messageUser.toLowerCase().trim();
        String etat = contextEmotionnel.toLowerCase();

        if (msg.matches(".*(bonjour|salut|hello|coucou|hey).*")) {
            if (etat.contains("stresse"))
                return "Bonjour \uD83D\uDC9C Je vois que tu traverses un moment difficile. Respire, on y va doucement.";
            if (etat.contains("fatigue"))
                return "Bonjour \uD83C\uDF19 Tu sembles fatigue(e). Commence petit — une tache simple, puis une pause.";
            if (etat.contains("energise"))
                return "Bonjour ! \u26A1 Tu es en pleine forme — c'est le moment d'attaquer tes objectifs !";
            return "Bonjour ! \uD83C\uDF3F Comment puis-je t'aider a organiser ta journee ?";
        }

        if (msg.matches(".*(stress|anxieu|peur|angoiss|debord|overwhelm).*")) {
            return "Je comprends \uD83D\uDC9C Respire. AURA a reorganise tes taches pour alleger ta charge. Une seule chose a la fois.";
        }

        if (msg.matches(".*(fatigu|epuis|dormir|sommeil|tired|sleep).*")) {
            return "Tu meriterais du repos \uD83C\uDF19 Commence par 15 minutes sur la tache la plus simple, puis pause. Ton energie reviendra.";
        }

        if (msg.matches(".*(revision|etudi|travaill|commenc|study|work).*")) {
            if (etat.contains("stresse"))
                return "On y va doucement \uD83D\uDCAA 25 minutes sur le sujet le plus simple. Chaque petit pas compte !";
            if (etat.contains("energise"))
                return "C'est le moment parfait ! \uD83D\uDE80 Tu es focus — attaque le sujet difficile pendant que tu es dans la zone.";
            return "Bonne idee ! \uD83D\uDCDA Commence par tes priorites et fais une pause toutes les 25 minutes.";
        }

        if (msg.matches(".*(motiv|courage|peux pas|difficile|dur|hard|abandonn).*")) {
            return "Tu es plus fort(e) que tu ne le penses \uD83D\uDCAA AURA croit en toi. Une tache a la fois !";
        }

        if (msg.matches(".*(tache|task|priorite|organis|planning|agenda|todo).*")) {
            if (etat.contains("stresse"))
                return "J'ai reorganise tes taches \uD83D\uDCCB Les plus urgentes sont en haut, les secondaires reportees.";
            if (etat.contains("energise"))
                return "Tes taches sont pres ! \u26A1 Commence par les deadlines les plus proches pendant que tu es dans la zone.";
            return "Tes taches prioritaires sont prets \uD83D\uDCCB Commence par celles avec les deadlines les plus proches.";
        }

        if (msg.matches(".*(merci|thanks|super|genial|parfait|top).*")) {
            return "Avec plaisir ! \uD83D\uDE0A Je suis la quand tu as besoin. Continue comme ca !";
        }

        // Reponse par defaut selon etat
        if (etat.contains("stresse"))
            return "Je t'ecoute \uD83D\uDC9C Tu n'es pas seul(e) — AURA adapte ton planning pour que tu te sentes moins deborde(e).";
        if (etat.contains("fatigue"))
            return "Prends soin de toi \uD83C\uDF19 Allons-y doucement aujourd'hui.";
        if (etat.contains("energise"))
            return "Tu es en pleine forme ! \u26A1 Dis-moi comment maximiser cette belle energie.";
        return "Je t'ecoute \uD83C\uDF3F Comment puis-je t'aider a mieux organiser ta journee ?";
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    /** Couleur de la barre historique selon le mood */
    private String getMoodColor(String mood) {
        if (mood == null) return "#1BBFA8";
        return switch (mood.toLowerCase().trim()) {
            case "energise", "\u00e9nergis\u00e9" -> "#1BBFA8";
            case "focalise", "focalis\u00e9"      -> "#6B5FD4";
            case "calme"                           -> "#1BBFA8";
            case "stresse",  "stress\u00e9"        -> "#E85D3A";
            case "fatigue",  "fatigu\u00e9"        -> "#6B5FD4";
            default                                -> "#1BBFA8";
        };
    }

    /** Hauteur de la barre historique selon le mood */
    private double getMoodHeight(String mood) {
        if (mood == null) return 90;
        return switch (mood.toLowerCase().trim()) {
            case "energise", "\u00e9nergis\u00e9" -> 140;
            case "focalise", "focalis\u00e9"      -> 120;
            case "calme"                           -> 100;
            case "stresse",  "stress\u00e9"        -> 70;
            case "fatigue",  "fatigu\u00e9"        -> 55;
            default                                -> 90;
        };
    }

    /** Emoji representant chaque etat */
    private String getEmojiEtat(EtatEmotionnel etat) {
        if (etat == null) return "\uD83C\uDF1F";
        return switch (etat) {
            case STRESSE  -> "\uD83D\uDC9C";   // 💜
            case FATIGUE  -> "\uD83C\uDF19";   // 🌙
            case CALME    -> "\uD83C\uDF3F";   // 🌿
            case ENERGISE -> "\u26A1";          // ⚡
        };
    }

    /** Convertit le Theme de AdaptiveWorkspaceManager en EtatEmotionnel */
    private EtatEmotionnel mapThemeToEtat(AdaptiveWorkspaceManager.Theme theme) {
        return switch (theme) {
            case STRESSE  -> EtatEmotionnel.STRESSE;
            case FATIGUE  -> EtatEmotionnel.FATIGUE;
            case CALME    -> EtatEmotionnel.CALME;
            case ENERGISE -> EtatEmotionnel.ENERGISE;
        };
    }

    /** Applique la couleur accent du theme sur le bouton micro */
    private void appliquerCouleursMicBtn(String accent) {
        micButton.setStyle(
                "-fx-background-color: " + accent + "; " +
                        "-fx-text-fill: #0A0C0F; " +
                        "-fx-background-radius: 40; -fx-padding: 20 24; " +
                        "-fx-font-size: 14; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, " + accent + ", 14, 0.4, 0, 2);"
        );
    }
    private void afficherReorganisation(ResultatReorganisation reo) {
        if (reorganisationPanel == null) return;

        reorganisationPanel.setVisible(true);
        reorganisationPanel.setManaged(true);

        if (reorganisationMessage != null)
            reorganisationMessage.setText(reo.messageUtilisateur);

        if (reorganisationConseil != null)
            reorganisationConseil.setText(reo.conseil);

        // Affiche les tâches recommandées
        if (tachesRecommandeesBox != null) {
            tachesRecommandeesBox.getChildren().clear();

            String accent = AdaptiveWorkspaceManager.getInstance().getAccentColor();
            String cardBg = AdaptiveWorkspaceManager.getInstance().getPaletteActuelle().cardBackground;

            for (Model.Task t : reo.tachesRecommandees) {
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-background-color: " + cardBg + "; " +
                                "-fx-background-radius: 10; -fx-padding: 10 14;"
                );

                // Dot couleur selon priorité
                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5);
                dot.setFill(javafx.scene.paint.Color.web(couleurPriorite(t.getPriority())));

                // Titre tâche
                javafx.scene.control.Label titre = new javafx.scene.control.Label(t.getTitle());
                titre.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
                javafx.scene.layout.HBox.setHgrow(titre, javafx.scene.layout.Priority.ALWAYS);

                // Badge priorité
                javafx.scene.control.Label badge = new javafx.scene.control.Label(t.getPriority());
                badge.setStyle(
                        "-fx-background-color: " + couleurPriorite(t.getPriority()) +
                                "33; -fx-text-fill: " + couleurPriorite(t.getPriority()) +
                                "; -fx-font-size: 10; -fx-background-radius: 6; -fx-padding: 2 8;"
                );

                row.getChildren().addAll(dot, titre, badge);
                tachesRecommandeesBox.getChildren().add(row);
            }

            // Message si tâches reportées
            if (!reo.tachesReportees.isEmpty()) {
                javafx.scene.control.Label reportees = new javafx.scene.control.Label(
                        "⏸ " + reo.tachesReportees.size() + " tâche(s) haute priorité reportées pour plus tard"
                );
                reportees.setStyle(
                        "-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 11; -fx-padding: 4 0 0 0;"
                );
                tachesRecommandeesBox.getChildren().add(reportees);
            }
        }
    }

    private String couleurPriorite(String priorite) {
        if (priorite == null) return "#A8B4C0";
        return switch (priorite.toLowerCase()) {
            case "haute"   -> "#E85D3A";
            case "moyenne" -> "#EF9F27";
            case "basse"   -> "#1BBFA8";
            default        -> "#A8B4C0";
        };
    }
    private String getFallbackMessage(String moodLabel) {
        return switch (moodLabel.toLowerCase().trim()) {
            case "stressé", "stresse"   ->
                    "Hey, respire. J'ai allégé ta liste pour toi. 💙";
            case "fatigué", "fatigue"   ->
                    "Tu as besoin de récupérer. Je garde l'essentiel pour aujourd'hui. 🌙";
            case "calme"                ->
                    "Parfait état pour avancer. Tes tâches sont prêtes. ✨";
            case "énergisé", "energise" ->
                    "Tu es en feu ! Tes priorités hautes sont devant. 🚀";
            default                     ->
                    "Je suis là pour t'aider. 💚";
        };
    }
}
