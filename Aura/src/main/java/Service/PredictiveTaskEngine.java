package Service;

import Model.Task;
import DAO.TaskDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * MÉTIER AVANCÉ #2 — Predictive Task Intelligence
 *
 * Analyse les tâches de l'utilisateur et prédit :
 *  - Risque de retard (0–100%)
 *  - Score de surcharge globale
 *  - Heure de productivité optimale
 *  - Recommandations de réorganisation
 */
public class PredictiveTaskEngine {

    // ── Résultat par tâche ───────────────────────────────────
    public static class PredictionTache {
        public final Task tache;
        public final int  risqueRetard;      // 0–100%
        public final String niveauRisque;    // "CRITIQUE", "ÉLEVÉ", "MODÉRÉ", "OK"
        public final String couleurRisque;   // couleur hex pour l'UI
        public final String conseil;         // conseil spécifique à la tâche

        public PredictionTache(Task tache, int risqueRetard, String niveauRisque,
                               String couleurRisque, String conseil) {
            this.tache        = tache;
            this.risqueRetard = risqueRetard;
            this.niveauRisque = niveauRisque;
            this.couleurRisque = couleurRisque;
            this.conseil      = conseil;
        }
    }

    // ── Résultat global ──────────────────────────────────────
    public static class RapportPredictif {
        public final List<PredictionTache> predictions;
        public final int  scoreChargeTravail;   // 0–100
        public final int  tachesEnRetard;
        public final int  tachesARisque;
        public final int  tachesOK;
        public final String heureOptimale;      // ex: "19h–22h"
        public final String resumeGlobal;       // affiché dans l'UI
        public final String recommandationGlobale;

        public RapportPredictif(List<PredictionTache> predictions,
                                int scoreChargeTravail,
                                int tachesEnRetard,
                                int tachesARisque,
                                int tachesOK,
                                String heureOptimale,
                                String resumeGlobal,
                                String recommandationGlobale) {
            this.predictions            = predictions;
            this.scoreChargeTravail     = scoreChargeTravail;
            this.tachesEnRetard         = tachesEnRetard;
            this.tachesARisque          = tachesARisque;
            this.tachesOK               = tachesOK;
            this.heureOptimale          = heureOptimale;
            this.resumeGlobal           = resumeGlobal;
            this.recommandationGlobale  = recommandationGlobale;
        }
    }

    private final TaskDAO taskDAO;

    public PredictiveTaskEngine() {
        this.taskDAO = new TaskDAO();
    }

    // ─────────────────────────────────────────────────────────
    // MÉTHODE PRINCIPALE
    // ─────────────────────────────────────────────────────────
    public RapportPredictif analyser(int userId) {
        List<Task> toutesLesTaches = taskDAO.getAll(userId);

        // Filtre — on analyse seulement les tâches non terminées
        List<Task> tachesActives = new ArrayList<>();
        for (Task t : toutesLesTaches) {
            if (!"terminé".equalsIgnoreCase(t.getStatus())) {
                tachesActives.add(t);
            }
        }

        List<PredictionTache> predictions = new ArrayList<>();
        int enRetard  = 0;
        int aRisque   = 0;
        int ok        = 0;
        int scoreTotal = 0;

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

        // Score de charge globale
        int scoreCharge = tachesActives.isEmpty() ? 0 : scoreTotal / tachesActives.size();

        // Heure optimale basée sur l'heure actuelle et les habitudes simulées
        String heureOptimale = calculerHeureOptimale();

        // Résumé et recommandation globale
        String resume = genererResume(enRetard, aRisque, ok, scoreCharge);
        String recommandation = genererRecommandationGlobale(enRetard, aRisque, scoreCharge);

        System.out.println("=== PredictiveTaskEngine ===");
        System.out.println("Tâches analysées : " + tachesActives.size());
        System.out.println("En retard : " + enRetard + " | À risque : " + aRisque + " | OK : " + ok);
        System.out.println("Score charge : " + scoreCharge + "/100");

        return new RapportPredictif(predictions, scoreCharge,
                enRetard, aRisque, ok, heureOptimale, resume, recommandation);
    }

    // ─────────────────────────────────────────────────────────
    // PRÉDICTION PAR TÂCHE
    // ─────────────────────────────────────────────────────────
    private PredictionTache predireTache(Task tache) {
        int risque = 0;

        // 1. Score priorité (base)
        risque += switch (tache.getPriority().toLowerCase()) {
            case "haute"   -> 40;
            case "moyenne" -> 20;
            default        -> 5;
        };

        // 2. Score deadline
        long joursRestants = calculerJoursRestants(tache.getScheduledAt());
        if      (joursRestants < 0)  risque += 50; // déjà en retard
        else if (joursRestants == 0) risque += 40; // deadline aujourd'hui
        else if (joursRestants == 1) risque += 30; // demain
        else if (joursRestants <= 3) risque += 20; // dans 3 jours
        else if (joursRestants <= 7) risque += 10; // cette semaine
        else                         risque += 0;  // ok

        // 3. Bonus si "en cours" depuis longtemps (statut bloqué)
        if ("en cours".equalsIgnoreCase(tache.getStatus())) {
            risque += 10;
        }

        risque = Math.min(100, risque);

        // Niveau de risque
        String niveau;
        String couleur;
        if      (risque >= 75) { niveau = "CRITIQUE"; couleur = "#E85D3A"; }
        else if (risque >= 50) { niveau = "ÉLEVÉ";    couleur = "#EF9F27"; }
        else if (risque >= 25) { niveau = "MODÉRÉ";   couleur = "#6B5FD4"; }
        else                   { niveau = "OK";        couleur = "#1BBFA8"; }

        String conseil = genererConseilTache(tache, niveau, joursRestants);

        return new PredictionTache(tache, risque, niveau, couleur, conseil);
    }

    // ─────────────────────────────────────────────────────────
    // CALCUL JOURS RESTANTS
    // ─────────────────────────────────────────────────────────
    private long calculerJoursRestants(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBlank()) return 7; // pas de deadline = 7j par défaut

        try {
            LocalDate deadline;
            // Supporte "yyyy-MM-dd" et "yyyy-MM-dd HH:mm:ss"
            if (scheduledAt.length() > 10) {
                deadline = LocalDateTime.parse(scheduledAt,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
            } else {
                deadline = LocalDate.parse(scheduledAt);
            }
            return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        } catch (Exception e) {
            return 7;
        }
    }

    // ─────────────────────────────────────────────────────────
    // HEURE OPTIMALE DE PRODUCTIVITÉ
    // ─────────────────────────────────────────────────────────
    private String calculerHeureOptimale() {
        int heure = LocalDateTime.now().getHour();
        if      (heure >= 6  && heure < 10) return "9h–12h (matin)";
        else if (heure >= 10 && heure < 14) return "14h–17h (après-midi)";
        else if (heure >= 14 && heure < 19) return "19h–22h (soirée)";
        else                                return "9h–12h (demain matin)";
    }

    // ─────────────────────────────────────────────────────────
    // GÉNÉRATION DES TEXTES
    // ─────────────────────────────────────────────────────────
    private String genererConseilTache(Task tache, String niveau, long jours) {
        return switch (niveau) {
            case "CRITIQUE" -> jours < 0
                    ? "⚠️ En retard de " + Math.abs(jours) + " jour(s) ! Traite cette tâche immédiatement."
                    : "🔴 Deadline aujourd'hui ! Concentre-toi sur cette tâche en priorité absolue.";
            case "ÉLEVÉ" -> "🟠 Deadline dans " + jours + " jour(s). Bloque du temps aujourd'hui pour avancer.";
            case "MODÉRÉ" -> "🟣 Deadline dans " + jours + " jour(s). Planifie-la cette semaine.";
            default -> "✅ Sous contrôle. Continue à ce rythme.";
        };
    }

    private String genererResume(int enRetard, int aRisque, int ok, int scoreCharge) {
        if (enRetard > 0)
            return enRetard + " tâche(s) critique(s) · " + aRisque + " à risque · Score charge : " + scoreCharge + "/100";
        if (aRisque > 0)
            return aRisque + " tâche(s) à surveiller · " + ok + " sous contrôle · Score : " + scoreCharge + "/100";
        return "Tout est sous contrôle ✅ · Score charge : " + scoreCharge + "/100";
    }

    private String genererRecommandationGlobale(int enRetard, int aRisque, int scoreCharge) {
        if (scoreCharge >= 75)
            return "🔴 Surcharge détectée ! AURA recommande de reporter les tâches basses priorités et de te concentrer sur les critiques.";
        if (enRetard > 0)
            return "⚠️ Tu as " + enRetard + " tâche(s) en retard. Commence par elles dès maintenant avant d'ajouter de nouvelles tâches.";
        if (aRisque > 0)
            return "🟠 " + aRisque + " tâche(s) risquent d'être en retard cette semaine. Planifie-les en priorité.";
        return "✅ Ta charge de travail est bien équilibrée. Continue à ce rythme !";
    }
}