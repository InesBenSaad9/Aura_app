package Service;

import DAO.TaskDAO;
import Model.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MÉTIER AVANCÉ #1 — Smart Emotional Productivity Engine
 * Module : Réorganisation intelligente des tâches
 *
 * Selon l'humeur détectée, AURA réordonne les tâches "à faire"
 * et "en cours" pour protéger le bien-être de l'utilisateur.
 *
 *  STRESSÉ  → tâches basses priorité en premier, haute reportées
 *  FATIGUÉ  → uniquement les tâches simples visibles
 *  CALME    → ordre standard (haute → moyenne → basse)
 *  ENERGISÉ → tâches haute priorité en premier pour profiter de l'élan
 */
public class TaskReorganizationEngine {

    private final TaskDAO taskDAO = new TaskDAO();

    // ── Résultat de réorganisation ────────────────────────────
    public static class ResultatReorganisation {
        public final List<Task> tachesRecommandees;   // à faire maintenant
        public final List<Task> tachesReportees;       // à éviter pour l'instant
        public final String     messageUtilisateur;    // affiché dans l'UI
        public final String     conseil;               // conseil court

        public ResultatReorganisation(List<Task> recommandees, List<Task> reportees,
                                      String message, String conseil) {
            this.tachesRecommandees = recommandees;
            this.tachesReportees    = reportees;
            this.messageUtilisateur = message;
            this.conseil            = conseil;
        }
    }

    // ─────────────────────────────────────────────────────────
    // MÉTHODE PRINCIPALE
    // ─────────────────────────────────────────────────────────

    /**
     * @param userId    ID de l'utilisateur connecté
     * @param moodLabel humeur brute de VoiceAnalyzer
     * @return ResultatReorganisation avec listes triées + messages
     */
    public ResultatReorganisation reorganiser(int userId, String moodLabel) {
        List<Task> toutes = taskDAO.getAll(userId);

        // Filtre uniquement les tâches actives (pas terminées)
        List<Task> actives = toutes.stream()
                .filter(t -> !t.getStatus().equalsIgnoreCase("terminé"))
                .toList();

        if (actives.isEmpty()) {
            return new ResultatReorganisation(
                    new ArrayList<>(), new ArrayList<>(),
                    "Aucune tâche active — profite de ta journée ! 🎉",
                    "Tu es à jour !"
            );
        }

        String humeur = moodLabel != null ? moodLabel.toLowerCase().trim() : "calme";

        return switch (humeur) {
            case "stressé",  "stresse"              -> reorganiserStresse(actives);
            case "fatigué",  "fatigue"              -> reorganiserFatigue(actives);
            case "énergisé", "energisé", "energise" -> reorganiserEnergise(actives);
            default                                 -> reorganiserCalme(actives);
        };
    }

    // ─────────────────────────────────────────────────────────
    // STRESSÉ — commence par le plus simple pour réduire l'anxiété
    // ─────────────────────────────────────────────────────────
    private ResultatReorganisation reorganiserStresse(List<Task> actives) {
        List<Task> recommandees = new ArrayList<>();
        List<Task> reportees    = new ArrayList<>();

        for (Task t : actives) {
            if (t.getPriority().equalsIgnoreCase("haute")) {
                reportees.add(t);    // reporte les tâches lourdes
            } else {
                recommandees.add(t); // basse + moyenne en premier
            }
        }

        // Trie : moyenne avant basse, puis par date
        recommandees.sort(Comparator
                .comparingInt((Task t) -> prioriteScore(t.getPriority()))
                .reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : "")
        );

        int nb = recommandees.size();
        String message = nb == 0
                ? "Toutes tes tâches sont importantes — prends une pause d'abord 🧘"
                : "AURA a allégé ta liste — " + nb + " tâche(s) accessible(s) maintenant";

        return new ResultatReorganisation(
                recommandees, reportees,
                message,
                "💜 Mode Zen : commence par une petite tâche. Chaque pas compte."
        );
    }

    // ─────────────────────────────────────────────────────────
    // FATIGUÉ — montre seulement les tâches basse priorité
    // ─────────────────────────────────────────────────────────
    private ResultatReorganisation reorganiserFatigue(List<Task> actives) {
        List<Task> recommandees = new ArrayList<>();
        List<Task> reportees    = new ArrayList<>();

        for (Task t : actives) {
            if (t.getPriority().equalsIgnoreCase("basse")) {
                recommandees.add(t);
            } else {
                reportees.add(t); // haute + moyenne reportées
            }
        }

        // Si rien de basse priorité, prend 1 seule tâche moyenne
        if (recommandees.isEmpty()) {
            List<Task> moyennes = actives.stream()
                    .filter(t -> t.getPriority().equalsIgnoreCase("moyenne"))
                    .limit(1)
                    .toList();
            recommandees.addAll(moyennes);
            reportees.removeAll(moyennes);
        }

        String message = recommandees.isEmpty()
                ? "Tu mérites une vraie pause — tes tâches peuvent attendre 🌙"
                : "AURA te suggère " + recommandees.size() + " tâche(s) légère(s) pour aujourd'hui";

        return new ResultatReorganisation(
                recommandees, reportees,
                message,
                "🌸 Mode Doux : repose-toi, les grandes tâches attendent demain."
        );
    }

    // ─────────────────────────────────────────────────────────
    // CALME — ordre standard par priorité
    // ─────────────────────────────────────────────────────────
    private ResultatReorganisation reorganiserCalme(List<Task> actives) {
        List<Task> triees = new ArrayList<>(actives);
        triees.sort(Comparator
                .comparingInt((Task t) -> prioriteScore(t.getPriority()))
                .reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : "")
        );

        return new ResultatReorganisation(
                triees, new ArrayList<>(),
                "Planning équilibré — " + triees.size() + " tâche(s) organisées par priorité",
                "🍃 Mode Calme : avance à ton rythme, tu gères bien."
        );
    }

    // ─────────────────────────────────────────────────────────
    // ÉNERGISÉ — tâches haute priorité en premier, max d'impact
    // ─────────────────────────────────────────────────────────
    private ResultatReorganisation reorganiserEnergise(List<Task> actives) {
        List<Task> triees = new ArrayList<>(actives);

        // Haute → moyenne → basse, puis deadline la plus proche
        triees.sort(Comparator
                .comparingInt((Task t) -> prioriteScore(t.getPriority()))
                .reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : "9999")
        );

        // Sépare haute priorité comme "recommandées en premier"
        List<Task> recommandees = triees.stream()
                .filter(t -> t.getPriority().equalsIgnoreCase("haute"))
                .toList();
        List<Task> reste = triees.stream()
                .filter(t -> !t.getPriority().equalsIgnoreCase("haute"))
                .toList();

        List<Task> toutes = new ArrayList<>(recommandees);
        toutes.addAll(reste);

        return new ResultatReorganisation(
                toutes, new ArrayList<>(),
                "⚡ Mode Boost : " + recommandees.size() + " tâche(s) haute priorité en avant !",
                "⚡ Tu es dans la zone — attaque les deadlines importantes maintenant !"
        );
    }

    // ─────────────────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────────────────

    /** Convertit la priorité en score numérique pour le tri */
    private int prioriteScore(String priorite) {
        if (priorite == null) return 1;
        return switch (priorite.toLowerCase().trim()) {
            case "haute"   -> 3;
            case "moyenne" -> 2;
            case "basse"   -> 1;
            default        -> 1;
        };
    }

    /**
     * Retourne le nombre de tâches "à faire" haute priorité
     * Utilisé par TaskPredictionEngine pour le score de charge
     */
    public int getNbTachesHautePriorite(int userId) {
        return (int) taskDAO.getAll(userId).stream()
                .filter(t -> t.getPriority().equalsIgnoreCase("haute"))
                .filter(t -> !t.getStatus().equalsIgnoreCase("terminé"))
                .count();
    }
}