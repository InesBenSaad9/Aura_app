package tn.esprit.aura.services;

import tn.esprit.aura.dao.TaskDAO;
import tn.esprit.aura.entities.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Predictive Task Intelligence Engine.
 * Migrated from the Aura AI-core module (Service.PredictiveTaskEngine).
 * Now uses unified TaskDAO from tn.esprit.aura.dao.
 */
public class PredictiveTaskEngine {

    public static class PredictionTache {
        public final Task tache;
        public final int  risqueRetard;
        public final String niveauRisque;
        public final String couleurRisque;
        public final String conseil;

        public PredictionTache(Task tache, int risqueRetard, String niveauRisque,
                               String couleurRisque, String conseil) {
            this.tache = tache;
            this.risqueRetard = risqueRetard;
            this.niveauRisque = niveauRisque;
            this.couleurRisque = couleurRisque;
            this.conseil = conseil;
        }
    }

    public static class RapportPredictif {
        public final List<PredictionTache> predictions;
        public final int  scoreChargeTravail;
        public final int  tachesEnRetard;
        public final int  tachesARisque;
        public final int  tachesOK;
        public final String heureOptimale;
        public final String resumeGlobal;
        public final String recommandationGlobale;

        public RapportPredictif(List<PredictionTache> predictions, int scoreChargeTravail,
                                int tachesEnRetard, int tachesARisque, int tachesOK,
                                String heureOptimale, String resumeGlobal, String recommandationGlobale) {
            this.predictions = predictions;
            this.scoreChargeTravail = scoreChargeTravail;
            this.tachesEnRetard = tachesEnRetard;
            this.tachesARisque = tachesARisque;
            this.tachesOK = tachesOK;
            this.heureOptimale = heureOptimale;
            this.resumeGlobal = resumeGlobal;
            this.recommandationGlobale = recommandationGlobale;
        }
    }

    private final TaskDAO taskDAO;

    public PredictiveTaskEngine() {
        this.taskDAO = new TaskDAO();
    }

    public RapportPredictif analyser(int userId) {
        List<Task> toutesLesTaches = taskDAO.getAll(userId);
        List<Task> tachesActives = new ArrayList<>();
        for (Task t : toutesLesTaches) {
            if (!"terminé".equalsIgnoreCase(t.getStatus())) tachesActives.add(t);
        }

        List<PredictionTache> predictions = new ArrayList<>();
        int enRetard = 0, aRisque = 0, ok = 0, scoreTotal = 0;

        for (Task t : tachesActives) {
            PredictionTache pred = predireTache(t);
            predictions.add(pred);
            scoreTotal += pred.risqueRetard;
            switch (pred.niveauRisque) {
                case "CRITIQUE" -> enRetard++;
                case "ÉLEVÉ"    -> aRisque++;
                default         -> ok++;
            }
        }

        int scoreCharge = tachesActives.isEmpty() ? 0 : scoreTotal / tachesActives.size();
        String heureOptimale = calculerHeureOptimale();
        String resume = genererResume(enRetard, aRisque, ok, scoreCharge);
        String recommandation = genererRecommandationGlobale(enRetard, aRisque, scoreCharge);

        return new RapportPredictif(predictions, scoreCharge, enRetard, aRisque, ok,
                heureOptimale, resume, recommandation);
    }

    private PredictionTache predireTache(Task tache) {
        int risque = switch (tache.getPriority().toLowerCase()) {
            case "haute"   -> 40;
            case "moyenne" -> 20;
            default        -> 5;
        };

        long joursRestants = calculerJoursRestants(tache.getScheduledAt());
        if      (joursRestants < 0)   risque += 50;
        else if (joursRestants == 0)  risque += 40;
        else if (joursRestants == 1)  risque += 30;
        else if (joursRestants <= 3)  risque += 20;
        else if (joursRestants <= 7)  risque += 10;

        if ("en cours".equalsIgnoreCase(tache.getStatus())) risque += 10;
        risque = Math.min(100, risque);

        String niveau, couleur;
        if      (risque >= 75) { niveau = "CRITIQUE"; couleur = "#E85D3A"; }
        else if (risque >= 50) { niveau = "ÉLEVÉ";    couleur = "#EF9F27"; }
        else if (risque >= 25) { niveau = "MODÉRÉ";   couleur = "#6B5FD4"; }
        else                   { niveau = "OK";        couleur = "#1BBFA8"; }

        return new PredictionTache(tache, risque, niveau, couleur,
                genererConseilTache(tache, niveau, joursRestants));
    }

    private long calculerJoursRestants(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBlank()) return 7;
        try {
            LocalDate deadline = scheduledAt.length() > 10
                    ? LocalDateTime.parse(scheduledAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate()
                    : LocalDate.parse(scheduledAt);
            return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        } catch (Exception e) { return 7; }
    }

    private String calculerHeureOptimale() {
        int heure = LocalDateTime.now().getHour();
        if      (heure >= 6  && heure < 10) return "9h–12h (matin)";
        else if (heure >= 10 && heure < 14) return "14h–17h (après-midi)";
        else if (heure >= 14 && heure < 19) return "19h–22h (soirée)";
        else                                return "9h–12h (demain matin)";
    }

    private String genererConseilTache(Task tache, String niveau, long jours) {
        return switch (niveau) {
            case "CRITIQUE" -> jours < 0
                    ? "⚠️ En retard de " + Math.abs(jours) + " jour(s) ! Traite cette tâche immédiatement."
                    : "🔴 Deadline aujourd'hui ! Concentre-toi sur cette tâche en priorité absolue.";
            case "ÉLEVÉ"  -> "🟠 Deadline dans " + jours + " jour(s). Bloque du temps aujourd'hui pour avancer.";
            case "MODÉRÉ" -> "🟣 Deadline dans " + jours + " jour(s). Planifie-la cette semaine.";
            default       -> "✅ Sous contrôle. Continue à ce rythme.";
        };
    }

    private String genererResume(int enRetard, int aRisque, int ok, int scoreCharge) {
        if (enRetard > 0) return enRetard + " tâche(s) critique(s) · " + aRisque + " à risque · Score charge : " + scoreCharge + "/100";
        if (aRisque  > 0) return aRisque  + " tâche(s) à surveiller · " + ok + " sous contrôle · Score : " + scoreCharge + "/100";
        return "Tout est sous contrôle ✅ · Score charge : " + scoreCharge + "/100";
    }

    private String genererRecommandationGlobale(int enRetard, int aRisque, int scoreCharge) {
        if (scoreCharge >= 75) return "🔴 Surcharge détectée ! AURA recommande de reporter les tâches basses priorités.";
        if (enRetard > 0)      return "⚠️ Tu as " + enRetard + " tâche(s) en retard. Commence par elles dès maintenant.";
        if (aRisque > 0)       return "🟠 " + aRisque + " tâche(s) risquent d'être en retard cette semaine. Planifie-les.";
        return "✅ Ta charge de travail est bien équilibrée. Continue à ce rythme !";
    }
}
