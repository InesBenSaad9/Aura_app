package tn.esprit.aura.services;

import java.io.*;

/**
 * WhisperService — Speech-to-text via local Python/Whisper script.
 * Migrated from the Aura AI-core module (Service.WhisperService).
 * The Python script path is now relative to the project root.
 */
public class WhisperService {

    public String transcribe(String audioFilePath) {
        try {
            // Use "py" on Windows, "python3" on Linux/Mac
            ProcessBuilder pb = new ProcessBuilder(
                    "py",
                    "whisper_transcribe.py",
                    audioFilePath
            );
            pb.redirectErrorStream(false);
            // Run from the project working directory (where the .py script lives)
            pb.directory(new File(System.getProperty("user.dir")));
            Process process = pb.start();

            // Drain stderr to avoid blocking
            new Thread(() -> {
                try {
                    BufferedReader errReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream())
                    );
                    while (errReader.readLine() != null) {}
                } catch (Exception ignored) {}
            }).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
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
