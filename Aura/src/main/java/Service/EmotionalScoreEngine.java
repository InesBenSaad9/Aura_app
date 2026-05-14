package Service;

import DAO.MoodDAO;
import Model.Mood;

import java.util.List;

/**
 * MÉTIER AVANCÉ #1 — Smart Emotional Productivity Engine
 *
 * Analyse l'état émotionnel de l'utilisateur à partir de :
 *  - Le label de la dernière humeur (détecté par VoiceAnalyzer)
 *  - Le niveau d'énergie brut (détecté par VoiceAnalyzer)
 *  - L'historique des 7 dernières humeurs
 *
 * Retourne :
 *  - Un score émotionnel 0–100
 *  - Un état : STRESSE / NEUTRE / PRODUCTIF
 *  - Une recommandation textuelle
 *  - Un thème UI à appliquer (pour AdaptiveWorkspaceManager)
 */
public class EmotionalScoreEngine {

    // ── Seuils ──────────────────────────────────────────────
    private static final int SEUIL_STRESSE  = 35;
    private static final int SEUIL_PRODUCTIF = 65;

    // ── État émotionnel ──────────────────────────────────────
    public enum EtatEmotionnel {
        STRESSE, FATIGUE, CALME, ENERGISE
    }

    // ── Thème UI ─────────────────────────────────────────────
    public enum ThemeUI {
        ZEN,    // Bloom — stressé
        NEUTRE, // Breathe — calme
        BOOST   // Toxic Pulse — énergisé
    }

    // ── Résultat complet ─────────────────────────────────────
    public static class ResultatEmotionnel {
        public final int score;                    // 0–100
        public final EtatEmotionnel etat;
        public final ThemeUI theme;
        public final String recommandation;
        public final String messageUI;             // affiché dans l'orbe

        public ResultatEmotionnel(int score, EtatEmotionnel etat, ThemeUI theme,
                                  String recommandation, String messageUI) {
            this.score = score;
            this.etat = etat;
            this.theme = theme;
            this.recommandation = recommandation;
            this.messageUI = messageUI;
        }
    }

    private final MoodDAO moodDAO;

    public EmotionalScoreEngine() {
        this.moodDAO = new MoodDAO();
    }

    // ─────────────────────────────────────────────────────────
    // MÉTHODE PRINCIPALE — appelle après chaque analyse vocale
    // ─────────────────────────────────────────────────────────

    /**
     * @param userId      ID de l'utilisateur connecté
     * @param moodLabel   label retourné par VoiceAnalyzer (ex: "stressé")
     * @param rawEnergy   énergie brute retournée par VoiceAnalyzer (0–1500+)
     */
    public ResultatEmotionnel analyser(int userId, String moodLabel, double rawEnergy) {

        // 1. Score du label humeur (60% du score final)
        int scoreMood = scorerMoodLabel(moodLabel);

        // 2. Score de l'énergie vocale (25% du score final)
        int scoreEnergie = scorerEnergie(rawEnergy);

        // 3. Bonus/malus historique des 7 derniers jours (15%)
        int scoreHistorique = scorerHistorique(userId);

        // 4. Score final pondéré
        int scoreFinal = (int) (scoreMood * 0.60 + scoreEnergie * 0.25 + scoreHistorique * 0.15);
        scoreFinal = Math.max(0, Math.min(100, scoreFinal)); // clamp 0–100

        // 5. Détermination de l'état (4 états)
        EtatEmotionnel etat;
        if      (scoreFinal < 35) etat = EtatEmotionnel.STRESSE;
        else if (scoreFinal < 45) etat = EtatEmotionnel.FATIGUE;
        else if (scoreFinal < 65) etat = EtatEmotionnel.CALME;
        else                      etat = EtatEmotionnel.ENERGISE;

        // 6. Thème UI correspondant
        ThemeUI theme = switch (etat) {
            case STRESSE  -> ThemeUI.ZEN;
            case ENERGISE -> ThemeUI.BOOST;
            default       -> ThemeUI.NEUTRE;
        };

        // 7. Recommandation + message orbe
        String recommandation = genererRecommandation(etat, moodLabel, scoreFinal);
        String messageUI      = genererMessageUI(etat, scoreFinal);

        System.out.println("=== EmotionalScoreEngine ===");
        System.out.println("Score : " + scoreFinal + "/100 | État : " + etat + " | Thème : " + theme);
        System.out.println("Recommandation : " + recommandation);

        return new ResultatEmotionnel(scoreFinal, etat, theme, recommandation, messageUI);
    }

    // ─────────────────────────────────────────────────────────
    // SCORING INTERNE
    // ─────────────────────────────────────────────────────────

    /**
     * Convertit le label humeur en score 0–100
     * Labels possibles depuis VoiceAnalyzer : fatigué, stressé, énergisé, focalisé, calme
     */
    private int scorerMoodLabel(String label) {
        if (label == null) return 50;
        return switch (label.toLowerCase().trim()) {
            case "énergisé"  -> 90;
            case "focalisé"  -> 80;
            case "calme"     -> 65;
            case "fatigué"   -> 30;
            case "stressé"   -> 20;
            default          -> 50;
        };
    }

    /**
     * Convertit l'énergie brute (0–1500+) en score 0–100
     * Basé sur les seuils de VoiceAnalyzer : <300 fatigué, >700 stressé, >900 énergisé
     */
    private int scorerEnergie(double rawEnergy) {
        if (rawEnergy < 100)  return 10;
        if (rawEnergy < 300)  return 25;
        if (rawEnergy < 500)  return 50;
        if (rawEnergy < 700)  return 65;
        if (rawEnergy < 900)  return 75;
        return 85; // énergie très haute = énergisé
    }

    /**
     * Analyse les 7 derniers moods pour détecter une tendance
     * Malus si beaucoup de "stressé" / "fatigué" récemment
     */
    private int scorerHistorique(int userId) {
        try {
            List<Mood> historique = moodDAO.getAll(userId);
            if (historique.isEmpty()) return 50;

            // Prend les 7 derniers max
            int limite = Math.min(7, historique.size());
            int scoreTotal = 0;
            for (int i = 0; i < limite; i++) {
                scoreTotal += scorerMoodLabel(historique.get(i).getLabel());
            }
            return scoreTotal / limite;

        } catch (Exception e) {
            System.out.println("Erreur historique : " + e.getMessage());
            return 50; // score neutre par défaut
        }
    }

    // ─────────────────────────────────────────────────────────
    // GÉNÉRATION DES TEXTES
    // ─────────────────────────────────────────────────────────

    private String genererRecommandation(EtatEmotionnel etat, String moodLabel, int score) {
        return switch (etat) {
            case STRESSE -> "Niveau de stress élevé (score : " + score + "/100). AURA a mis en pause tes tâches secondaires. Respire, et concentre-toi sur une seule chose à la fois.";
            case FATIGUE -> "Tu sembles fatiguée aujourd'hui (score : " + score + "/100). AURA a réorganisé tes tâches — commence par les plus simples. Pense à faire une pause de 15 minutes.";
            case CALME   -> "Tu es dans un bon équilibre (score : " + score + "/100). Continue ainsi — AURA garde un œil sur ta charge de travail.";
            case ENERGISE -> "Excellente énergie ! (score : " + score + "/100) C'est le moment parfait pour attaquer tes tâches les plus difficiles.";
        };
    }

    private String genererMessageUI(EtatEmotionnel etat, int score) {
        return switch (etat) {
            case STRESSE  -> "Mode Zen activé — Score " + score + "/100";
            case FATIGUE  -> "Mode Doux activé — Score " + score + "/100";
            case CALME    -> "Mode Breathe — Score " + score + "/100";
            case ENERGISE -> "Mode Boost activé — Score " + score + "/100";
        };
    }

    // ─────────────────────────────────────────────────────────
    // UTILITAIRE — réordonne les tâches selon l'état
    // ─────────────────────────────────────────────────────────

    /**
     * Retourne true si les tâches doivent être réorganisées
     * (tâches simples en premier quand l'utilisateur est stressée)
     */
    public boolean doitReorganiserTaches(EtatEmotionnel etat) {
        return etat == EtatEmotionnel.STRESSE;
    }

    /**
     * Retourne la couleur de l'orbe selon l'état émotionnel
     * Compatible avec les couleurs de ta palette AURA
     */
    public String getCouleurOrbe(EtatEmotionnel etat) {
        return switch (etat) {
            case STRESSE  -> "#4A3F8F"; // violet sombre — zen
            case FATIGUE  -> "#C8A0D4"; // lavande douce
            case CALME    -> "#6B9E8A"; // sage green
            case ENERGISE -> "#1BBFA8"; // teal vif — boost
        };
    }
}