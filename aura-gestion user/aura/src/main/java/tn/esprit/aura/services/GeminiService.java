package tn.esprit.aura.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * GeminiService — Génère une réponse intelligente via Google Gemini.
 * Migrated from the Aura AI-core module (Service.GeminiService).
 */
public class GeminiService {

    private static final String API_KEY = "AIzaSyBcQXAnzXgIoM-hXsfx_TsXsHElc_1hQ8g";

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String genererReponse(String transcription, String moodLabel) {
        try {
            String prompt = buildPrompt(transcription, moodLabel);
            String jsonBody = "{"
                    + "\"contents\": [{"
                    + "  \"parts\": [{\"text\": " + toJsonString(prompt) + "}]"
                    + "}],"
                    + "\"generationConfig\": {"
                    + "  \"maxOutputTokens\": 80,"
                    + "  \"temperature\": 0.7"
                    + "}"
                    + "}";

            String response = httpPost(jsonBody);
            if (response == null) return getFallbackReponse(moodLabel);

            String reponse = extraireReponse(response);
            System.out.println("[Gemini] Réponse : " + reponse);
            return reponse;

        } catch (Exception e) {
            System.err.println("[Gemini] Erreur : " + e.getMessage());
            return getFallbackReponse(moodLabel);
        }
    }

    private String buildPrompt(String transcription, String moodLabel) {
        String contexte = switch (moodLabel == null ? "" : moodLabel.toLowerCase().trim()) {
            case "stressé", "stresse"   -> "L'utilisateur est stressé. Réponds de façon très douce et rassurante. Propose de souffler.";
            case "fatigué", "fatigue"   -> "L'utilisateur est fatigué. Sois calme et encourageante. Suggère une pause.";
            case "calme"                -> "L'utilisateur est calme. Sois positive et aide-le à rester focus.";
            case "énergisé", "energise" -> "L'utilisateur est plein d'énergie. Sois dynamique et enthousiaste !";
            case "focalisé", "focalise" -> "L'utilisateur est concentré. Sois précise et efficace.";
            default -> "Sois bienveillante et aide l'utilisateur.";
        };
        String texteUser = (transcription != null && !transcription.isBlank()) ? transcription : "Bonjour AURA";
        return "Tu es AURA, une assistante intelligente bienveillante. "
                + "Réponds en français, de façon courte (1-2 phrases max), chaleureuse. "
                + contexte
                + " L'utilisateur dit : \"" + texteUser + "\". "
                + "Réponds directement sans introduction.";
    }

    private String httpPost(String jsonBody) {
        try {
            URL url = new URL(GEMINI_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code != 200) {
                try (InputStream err = conn.getErrorStream()) {
                    if (err != null) System.err.println("[Gemini] Erreur : " + new String(err.readAllBytes()));
                }
                return null;
            }
            try (InputStream in = conn.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.err.println("[Gemini] Erreur HTTP : " + e.getMessage());
            return null;
        }
    }

    private String extraireReponse(String json) {
        try {
            String marker = "\"text\":\"";
            int start = json.indexOf(marker);
            if (start == -1) return getFallbackReponse(null);
            start += marker.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return getFallbackReponse(null);
            return json.substring(start, end).replace("\\n", " ").replace("\\\"", "\"").trim();
        } catch (Exception e) {
            return getFallbackReponse(null);
        }
    }

    private String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "\"";
    }

    private String getFallbackReponse(String moodLabel) {
        if (moodLabel == null) return "Je suis là pour toi. Comment puis-je t'aider ?";
        return switch (moodLabel.toLowerCase().trim()) {
            case "stressé", "stresse"   -> "Respire doucement. Je suis là, on y va ensemble.";
            case "fatigué", "fatigue"   -> "Tu mérites du repos. Commence par quelque chose de simple.";
            case "calme"                -> "Tu es dans un bel équilibre. Continue comme ça !";
            case "énergisé", "energise" -> "Tu es en pleine forme ! C'est le moment de briller !";
            default                     -> "Je suis AURA, je suis là pour t'aider.";
        };
    }
}
