package tn.esprit.aura.services;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptive workspace manager — changes the UI theme based on detected mood.
 * Migrated from the Aura AI-core module (Service.AdaptiveWorkspaceManager).
 * Now also usable from UserDashboardController.
 */
public class AdaptiveWorkspaceManager {

    // ── Singleton ─────────────────────────────────────────────
    private static AdaptiveWorkspaceManager instance;
    public static AdaptiveWorkspaceManager getInstance() {
        if (instance == null) instance = new AdaptiveWorkspaceManager();
        return instance;
    }

    public enum Theme { ENERGISE, FATIGUE, CALME, STRESSE }

    public static class Palette {
        public final String background, sidebar, accent, accentSoft, accentSecondary;
        public final String textPrimary, textSecondary, cardBackground, borderColor, glowColor;
        public final String nomTheme;

        public Palette(String background, String sidebar, String accent, String accentSoft,
                       String accentSecondary, String textPrimary, String textSecondary,
                       String cardBackground, String borderColor, String glowColor, String nomTheme) {
            this.background = background; this.sidebar = sidebar;
            this.accent = accent; this.accentSoft = accentSoft; this.accentSecondary = accentSecondary;
            this.textPrimary = textPrimary; this.textSecondary = textSecondary;
            this.cardBackground = cardBackground; this.borderColor = borderColor;
            this.glowColor = glowColor; this.nomTheme = nomTheme;
        }
    }

    public static final Palette PALETTE_INITIALE = new Palette(
            "#0A0C0F", "#111418", "#1BBFA8", "rgba(27,191,168,0.10)", "#6B5FD4",
            "#F4F7F8", "#A8B4C0", "#111418", "rgba(27,191,168,0.15)", "rgba(27,191,168,0.18)", "AURA Initial");

    public static final Palette PALETTE_ENERGISE = new Palette(
            "#080E08", "#0C160C", "#8eff01", "rgba(142,255,1,0.12)", "#8b53fe",
            "#E8FFD0", "#7ACC00", "#0F1A0F", "rgba(142,255,1,0.3)", "rgba(142,255,1,0.35)", "Toxic Pulse ⚡");

    public static final Palette PALETTE_FATIGUE = new Palette(
            "#130E18", "#1A1222", "#C8A0D4", "rgba(200,160,212,0.12)", "#F4B8C8",
            "#F0E4F4", "#9E86AA", "#1C1428", "rgba(200,160,212,0.25)", "rgba(200,160,212,0.3)", "Soft Glow 🌸");

    public static final Palette PALETTE_CALME = new Palette(
            "#0E1512", "#121C18", "#6B9E8A", "rgba(107,158,138,0.12)", "#E8A882",
            "#EEF4F0", "#7AA898", "#141E1A", "rgba(107,158,138,0.25)", "rgba(107,158,138,0.3)", "Breathe 🍃");

    public static final Palette PALETTE_STRESSE = new Palette(
            "#100D18", "#160F22", "#C8B4E8", "rgba(200,180,232,0.12)", "#F4B8C8",
            "#EDE8F4", "#A090C0", "#1A1230", "rgba(200,180,232,0.25)", "rgba(200,180,232,0.3)", "Bloom 🌿");

    private Theme   themeActuel     = Theme.CALME;
    private Palette paletteActuelle = PALETTE_INITIALE;
    private String  humeurActuelle  = "initial";

    private HBox         rootHBox;
    private VBox         sidebar;
    private Button       activeButton;
    private List<Button> navButtons = new ArrayList<>();
    private List<Runnable> listeners = new ArrayList<>();

    public void register(HBox rootHBox, VBox sidebar, List<Button> navButtons) {
        this.rootHBox   = rootHBox;
        this.sidebar    = sidebar;
        this.navButtons = navButtons;
    }

    public void setActiveButton(Button btn)    { this.activeButton = btn; }
    public void addListener(Runnable listener) { listeners.add(listener); }

    public void appliquerDepuisHumeur(String moodLabel) {
        if (moodLabel == null) return;
        String humeur = moodLabel.toLowerCase().trim();
        if (humeur.equals(humeurActuelle)) return;
        humeurActuelle = humeur;

        themeActuel = switch (humeur) {
            case "énergisé", "energisé", "energise" -> Theme.ENERGISE;
            case "fatigué",  "fatigue"              -> Theme.FATIGUE;
            case "stressé",  "stresse"              -> Theme.STRESSE;
            default                                 -> Theme.CALME;
        };

        paletteActuelle = switch (themeActuel) {
            case ENERGISE -> PALETTE_ENERGISE;
            case FATIGUE  -> PALETTE_FATIGUE;
            case STRESSE  -> PALETTE_STRESSE;
            default       -> PALETTE_CALME;
        };

        Platform.runLater(this::appliquerPaletteUI);
        jouerSon(humeur);
        notifierListeners();
    }

    public void appliquerTheme(EmotionalScoreEngine.ThemeUI themeUI) {
        String humeur = switch (themeUI) {
            case ZEN   -> "stressé";
            case BOOST -> "énergisé";
            default    -> "calme";
        };
        appliquerDepuisHumeur(humeur);
    }

    private void appliquerPaletteUI() {
        if (rootHBox == null) return;
        rootHBox.setStyle("-fx-background-color: " + paletteActuelle.background + ";");
        if (sidebar != null)
            sidebar.setStyle("-fx-background-color: " + paletteActuelle.sidebar + "; -fx-border-width: 0;");
        for (Button btn : navButtons) {
            if (btn == null) continue;
            btn.setStyle(btn == activeButton ? styleActif() : styleInactif());
        }
    }

    private String styleActif() {
        return "-fx-background-color: " + paletteActuelle.accentSoft + "; "
                + "-fx-text-fill: " + paletteActuelle.accent + "; "
                + "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; "
                + "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand; "
                + "-fx-border-color: " + paletteActuelle.accent + "; "
                + "-fx-border-width: 0 0 0 3; -fx-border-radius: 0; "
                + "-fx-effect: dropshadow(gaussian, " + paletteActuelle.glowColor + ", 10, 0.4, 0, 0);";
    }

    private String styleInactif() {
        return "-fx-background-color: transparent; "
                + "-fx-text-fill: " + paletteActuelle.textSecondary + "; "
                + "-fx-font-size: 14; -fx-alignment: CENTER_LEFT; "
                + "-fx-padding: 12 16; -fx-background-radius: 10; -fx-cursor: hand;";
    }

    private void jouerSon(String humeur) {
        try {
            String soundFile = switch (humeur) {
                case "énergisé", "energisé" -> "/sounds/boost_chime.mp3";
                case "fatigué",  "fatigue"  -> "/sounds/soft_chime.mp3";
                case "stressé",  "stresse"  -> "/sounds/zen_chime.mp3";
                default                     -> "/sounds/calm_chime.mp3";
            };
            var url = getClass().getResource(soundFile);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toString());
                clip.setVolume(0.35);
                clip.play();
            }
        } catch (Exception e) {
            System.out.println("Son ignoré : " + e.getMessage());
        }
    }

    private void notifierListeners() { for (Runnable r : listeners) r.run(); }

    public Palette getPaletteActuelle()    { return paletteActuelle; }
    public Theme   getThemeActuel()        { return themeActuel; }
    public String  getAccentColor()        { return paletteActuelle.accent; }
    public String  getAccentSecondary()    { return paletteActuelle.accentSecondary; }
    public String  getBackgroundColor()    { return paletteActuelle.background; }
    public String  getTextPrimaryColor()   { return paletteActuelle.textPrimary; }
    public String  getTextSecondaryColor() { return paletteActuelle.textSecondary; }
    public String  getGlowColor()          { return paletteActuelle.glowColor; }

    public String getCardStyle() {
        return "-fx-background-color: " + paletteActuelle.cardBackground + "; "
                + "-fx-background-radius: 16; "
                + "-fx-border-color: " + paletteActuelle.borderColor + "; "
                + "-fx-border-radius: 16; -fx-border-width: 1; -fx-padding: 22;";
    }
}
