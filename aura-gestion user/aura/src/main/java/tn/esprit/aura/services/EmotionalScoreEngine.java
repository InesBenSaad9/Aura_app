package tn.esprit.aura.services;

import tn.esprit.aura.dao.MoodDAO;
import tn.esprit.aura.entities.Mood;

import java.util.List;

/**
 * Smart Emotional Productivity Engine.
 * Migrated from the Aura AI-core module (Service.EmotionalScoreEngine).
 * Now uses unified MoodDAO from tn.esprit.aura.dao.
 */
public class EmotionalScoreEngine {

    private static final int SEUIL_STRESSE   = 35;
    private static final int SEUIL_PRODUCTIF = 65;

    public enum EtatEmotionnel { STRESSE, FATIGUE, CALME, ENERGISE }

    public enum ThemeUI {
        ZEN,    // Bloom — stressé
        NEUTRE, // Breathe — calme
        BOOST   // Toxic Pulse — énergisé
    }

    public static class ResultatEmotionnel {
        public final int score;
        public final EtatEmotionnel etat;
        public final ThemeUI theme;
        public final String recommandation;
        public final String messageUI;

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

    public ResultatEmotionnel analyser(int userId, String moodLabel, double rawEnergy) {
        int scoreMood      = scorerMoodLabel(moodLabel);
        int scoreEnergie   = scorerEnergie(rawEnergy);
        int scoreHistorique = scorerHistorique(userId);
        int scoreFinal     = (int) (scoreMood * 0.60 + scoreEnergie * 0.25 + scoreHistorique * 0.15);
        scoreFinal = Math.max(0, Math.min(100, scoreFinal));

        EtatEmotionnel etat;
        if      (scoreFinal < 35) etat = EtatEmotionnel.STRESSE;
        else if (scoreFinal < 45) etat = EtatEmotionnel.FATIGUE;
        else if (scoreFinal < 65) etat = EtatEmotionnel.CALME;
        else                      etat = EtatEmotionnel.ENERGISE;

        ThemeUI theme = switch (etat) {
            case STRESSE  -> ThemeUI.ZEN;
            case ENERGISE -> ThemeUI.BOOST;
            default       -> ThemeUI.NEUTRE;
        };

        String recommandation = genererRecommandation(etat, moodLabel, scoreFinal);
        String messageUI      = genererMessageUI(etat, scoreFinal);

        return new ResultatEmotionnel(scoreFinal, etat, theme, recommandation, messageUI);
    }

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

    private int scorerEnergie(double rawEnergy) {
        if (rawEnergy < 100)  return 10;
        if (rawEnergy < 300)  return 25;
        if (rawEnergy < 500)  return 50;
        if (rawEnergy < 700)  return 65;
        if (rawEnergy < 900)  return 75;
        return 85;
    }

    private int scorerHistorique(int userId) {
        try {
            List<Mood> historique = moodDAO.getAll(userId);
            if (historique.isEmpty()) return 50;
            int limite = Math.min(7, historique.size());
            int scoreTotal = 0;
            for (int i = 0; i < limite; i++) scoreTotal += scorerMoodLabel(historique.get(i).getLabel());
            return scoreTotal / limite;
        } catch (Exception e) {
            return 50;
        }
    }

    private String genererRecommandation(EtatEmotionnel etat, String moodLabel, int score) {
        return switch (etat) {
            case STRESSE  -> "Niveau de stress élevé (score : " + score + "/100). AURA a mis en pause tes tâches secondaires. Respire, et concentre-toi sur une seule chose à la fois.";
            case FATIGUE  -> "Tu sembles fatigué(e) aujourd'hui (score : " + score + "/100). AURA a réorganisé tes tâches — commence par les plus simples. Pense à faire une pause de 15 minutes.";
            case CALME    -> "Tu es dans un bon équilibre (score : " + score + "/100). Continue ainsi — AURA garde un œil sur ta charge de travail.";
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

    public boolean doitReorganiserTaches(EtatEmotionnel etat) { return etat == EtatEmotionnel.STRESSE; }

    public String getCouleurOrbe(EtatEmotionnel etat) {
        return switch (etat) {
            case STRESSE  -> "#4A3F8F";
            case FATIGUE  -> "#C8A0D4";
            case CALME    -> "#6B9E8A";
            case ENERGISE -> "#1BBFA8";
        };
    }
}
