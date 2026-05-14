package Controller;

import DAO.MoodDAO;
import DAO.TaskDAO;
import Interface.PlanningService;
import Model.Mood;
import Model.Task;

import java.util.ArrayList;
import java.util.List;

public class PlanningController implements PlanningService {

    private TaskDAO taskDAO = new TaskDAO();
    private MoodDAO moodDAO = new MoodDAO();

    public List<Task> generatePlanning(int userId, String mood) {
        List<Task> allTasks = taskDAO.getAll(userId);
        List<Task> planning = new ArrayList<>();

        System.out.println("\n📅 Planning adapté pour humeur : " + mood);

        switch (mood) {
            case "fatigué":
                // Tâches faciles en premier
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("basse")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("moyenne")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("haute")) planning.add(t);
                }
                System.out.println("😴 Tu es fatigué — tâches faciles d'abord !");
                break;

            case "stressé":
                // Tâches courtes en premier pour se sentir productif
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("basse")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("haute")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("moyenne")) planning.add(t);
                }
                System.out.println("😰 Tu es stressé — commence par les petites tâches !");
                break;

            case "énergisé":
                // Tâches difficiles en premier
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("haute")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("moyenne")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("basse")) planning.add(t);
                }
                System.out.println("⚡ Tu es énergisé — attaque les tâches difficiles !");
                break;

            case "focalisé":
                // Tâches importantes en premier
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("haute")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("moyenne")) planning.add(t);
                }
                for (Task t : allTasks) {
                    if (t.getPriority().equalsIgnoreCase("basse")) planning.add(t);
                }
                System.out.println("🎯 Tu es focalisé — parfait pour les tâches importantes !");
                break;

            case "calme":
            default:
                // Ordre normal
                planning.addAll(allTasks);
                System.out.println("😌 Tu es calme — planning normal !");
                break;
        }

        // Sauvegarder dans la base
        savePlanning(userId, mood, planning);

        return planning;
    }

    private void savePlanning(int userId, String mood, List<Task> planning) {
        StringBuilder plan = new StringBuilder();
        for (int i = 0; i < planning.size(); i++) {
            plan.append((i + 1)).append(". ").append(planning.get(i).getTitle());
            if (i < planning.size() - 1) plan.append(" → ");
        }

        DAO.PlanningSessionDAO planningDAO = new DAO.PlanningSessionDAO();
        planningDAO.save(userId, mood, plan.toString());
    }
}