package Service;

import Interface.Transcribable;

import java.io.*;

public class WhisperService implements Transcribable {

    public String transcribe(String audioFilePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "py",
                    "C:\\Users\\Ines\\Downloads\\Aura\\Aura\\src\\whisper_transcribe.py",
                    audioFilePath
            );
            pb.redirectErrorStream(false); // ← sépare stderr de stdout
            Process process = pb.start();
            pb.directory(new java.io.File("C:\\Users\\Ines\\Downloads\\Aura\\Aura"));
            pb.redirectErrorStream(true);

            // Lit seulement stdout (le vrai texte)
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            // Ignore stderr (les warnings Python)
            new Thread(() -> {
                try {
                    BufferedReader errReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream())
                    );
                    while (errReader.readLine() != null) {} // vide stderr
                } catch (Exception ignored) {}
            }).start();

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Filtre les lignes qui ne sont pas du texte transcrit
                if (!line.contains("Warning") && !line.contains("UserWarning")
                        && !line.contains("site-packages") && !line.contains("warnings.warn")
                        && !line.contains("Traceback") && !line.trim().isEmpty()) {
                    result.append(line).append(" ");
                }
            }

            process.waitFor();
            return result.toString().trim();

        } catch (Exception e) {
            System.out.println("Erreur Whisper : " + e.getMessage());
            return null;
        }
    }
}