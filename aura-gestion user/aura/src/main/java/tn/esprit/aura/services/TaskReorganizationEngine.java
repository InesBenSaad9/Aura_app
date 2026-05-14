package tn.esprit.aura.services;

import tn.esprit.aura.dao.TaskDAO;
import tn.esprit.aura.entities.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Smart task reorganization based on the user's detected mood.
 * Migrated from the Aura AI-core module (Service.TaskReorganizationEngine).
 * Now uses unified TaskDAO from tn.esprit.aura.dao.
 */
public class TaskReorganizationEngine {

    private final TaskDAO taskDAO = new TaskDAO();

    public static class ResultatReorganisation {
        public final List<Task> tachesRecommandees;
        public final List<Task> tachesReportees;
        public final String     messageUtilisateur;
        public final String     conseil;

        public ResultatReorganisation(List<Task> recommandees, List<Task> reportees,
                                      String message, String conseil) {
            this.tachesRecommandees = recommandees;
            this.tachesReportees    = reportees;
            this.messageUtilisateur = message;
            this.conseil            = conseil;
        }
    }

    public ResultatReorganisation reorganiser(int userId, String moodLabel) {
        List<Task> toutes  = taskDAO.getAll(userId);
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
            default                                  -> reorganiserCalme(actives);
        };
    }

    private ResultatReorganisation reorganiserStresse(List<Task> actives) {
        List<Task> recommandees = new ArrayList<>();
        List<Task> reportees    = new ArrayList<>();
        for (Task t : actives) {
            if (t.getPriority().equalsIgnoreCase("haute")) reportees.add(t);
            else recommandees.add(t);
        }
        recommandees.sort(Comparator.comparingInt((Task t) -> prioriteScore(t.getPriority())).reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : ""));
        String message = recommandees.isEmpty()
                ? "Toutes tes tâches sont importantes — prends une pause d'abord 🧘"
                : "AURA a allégé ta liste — " + recommandees.size() + " tâche(s) accessible(s) maintenant";
        return new ResultatReorganisation(recommandees, reportees, message,
                "💜 Mode Zen : commence par une petite tâche. Chaque pas compte.");
    }

    private ResultatReorganisation reorganiserFatigue(List<Task> actives) {
        List<Task> recommandees = new ArrayList<>();
        List<Task> reportees    = new ArrayList<>();
        for (Task t : actives) {
            if (t.getPriority().equalsIgnoreCase("basse")) recommandees.add(t);
            else reportees.add(t);
        }
        if (recommandees.isEmpty()) {
            List<Task> moyennes = actives.stream()
                    .filter(t -> t.getPriority().equalsIgnoreCase("moyenne")).limit(1).toList();
            recommandees.addAll(moyennes);
            reportees.removeAll(moyennes);
        }
        String message = recommandees.isEmpty()
                ? "Tu mérites une vraie pause — tes tâches peuvent attendre 🌙"
                : "AURA te suggère " + recommandees.size() + " tâche(s) légère(s) pour aujourd'hui";
        return new ResultatReorganisation(recommandees, reportees, message,
                "🌸 Mode Doux : repose-toi, les grandes tâches attendent demain.");
    }

    private ResultatReorganisation reorganiserCalme(List<Task> actives) {
        List<Task> triees = new ArrayList<>(actives);
        triees.sort(Comparator.comparingInt((Task t) -> prioriteScore(t.getPriority())).reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : ""));
        return new ResultatReorganisation(triees, new ArrayList<>(),
                "Planning équilibré — " + triees.size() + " tâche(s) organisées par priorité",
                "🍃 Mode Calme : avance à ton rythme, tu gères bien.");
    }

    private ResultatReorganisation reorganiserEnergise(List<Task> actives) {
        List<Task> triees = new ArrayList<>(actives);
        triees.sort(Comparator.comparingInt((Task t) -> prioriteScore(t.getPriority())).reversed()
                .thenComparing(t -> t.getScheduledAt() != null ? t.getScheduledAt() : "9999"));
        List<Task> recommandees = triees.stream().filter(t -> t.getPriority().equalsIgnoreCase("haute")).toList();
        List<Task> reste        = triees.stream().filter(t -> !t.getPriority().equalsIgnoreCase("haute")).toList();
        List<Task> toutes       = new ArrayList<>(recommandees);
        toutes.addAll(reste);
        return new ResultatReorganisation(toutes, new ArrayList<>(),
                "⚡ Mode Boost : " + recommandees.size() + " tâche(s) haute priorité en avant !",
                "⚡ Tu es dans la zone — attaque les deadlines importantes maintenant !");
    }

    private int prioriteScore(String priorite) {
        if (priorite == null) return 1;
        return switch (priorite.toLowerCase().trim()) {
            case "haute"   -> 3;
            case "moyenne" -> 2;
            default        -> 1;
        };
    }

    public int getNbTachesHautePriorite(int userId) {
        return (int) taskDAO.getAll(userId).stream()
                .filter(t -> t.getPriority().equalsIgnoreCase("haute"))
                .filter(t -> !t.getStatus().equalsIgnoreCase("terminé"))
                .count();
    }
}
