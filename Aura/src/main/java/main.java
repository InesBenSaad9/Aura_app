import DAO.DatabaseConnection;

import java.sql.Connection;
import Model.Task;
import DAO.TaskDAO;
import java.util.List;
import java.util.Scanner;
import Controller.PlanningController;
import DAO.MoodDAO;
import Model.Mood;
import Service.VoiceAnalyzer;
import Service.WhisperService;
import java.util.Scanner;
public class main {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            System.out.println("Connexion réussie !");
        } else {
            System.out.println("Connexion échouée.");
        }
        Scanner scanner = new Scanner(System.in);
        TaskDAO taskDAO = new TaskDAO();

        System.out.println("📝 Combien de tâches tu veux ajouter ?");
        int nombre = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < nombre; i++) {
            System.out.println("Titre de la tâche " + (i + 1) + " :");
            String titre = scanner.nextLine();

            System.out.println("Priorité (haute / moyenne / basse) :");
            String priorite = scanner.nextLine();

            System.out.println("Date (format: 2025-06-01) :");
            String date = scanner.nextLine();

            Task task = new Task(0, 1, titre, priorite, "à faire", date);
            taskDAO.add(task);
        }

        // Afficher toutes les tâches
        List<Task> tasks = taskDAO.getAll(1);
        System.out.println("\n📋 Tes tâches :");
        for (Task t : tasks) {
            System.out.println("  - " + t.getTitle() + " | " + t.getPriority() + " | " + t.getStatus());
        }

        // dans le main :
        MoodDAO moodDAO = new MoodDAO();

        // Ajouter une humeur
        Mood mood = new Mood(0, 1, "calme", "");
        moodDAO.add(mood);

        // Récupérer la dernière humeur
        Mood lastMood = moodDAO.getLastMood(1);
        System.out.println("Dernière humeur : " + lastMood.getLabel());
        // dans le main :
        //VoiceAnalyzer analyzer = new VoiceAnalyzer();
        //String mood1 = analyzer.analyzeVoiceMood();
        //System.out.println("Humeur détectée : " + mood1);

        // Envoyer à Whisper
        //WhisperService whisper = new WhisperService();
        //String texte = whisper.transcribe(analyzer.getAudioFilePath());
        //System.out.println(" Tu as dit : " + texte);

        VoiceAnalyzer analyzer = new VoiceAnalyzer();
        String moodDetected = analyzer.analyzeVoiceMood();
        System.out.println("😊 Humeur détectée : " + moodDetected);

        WhisperService whisper = new WhisperService();
        String texte = whisper.transcribe("voice_record.wav");
        System.out.println("📝 Tu as dit : " + texte);

// dans le main — après la détection d'humeur :
        PlanningController planning = new PlanningController();
        List<Task> monPlanning = planning.generatePlanning(1, moodDetected);

        System.out.println("\n📋 Ton planning du jour :");
        for (int i = 0; i < monPlanning.size(); i++) {
            System.out.println((i + 1) + ". " + monPlanning.get(i).getTitle()
                    + " [" + monPlanning.get(i).getPriority() + "]");
        }

    }

   }


