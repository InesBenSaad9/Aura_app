package tn.esprit.aura.services;

import javax.sound.sampled.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * ElevenLabsService — Text-to-Speech pour AURA.
 * Migrated from the Aura AI-core module (Service.ElevenLabsService).
 */
public class ElevenLabsService {

    private static final String API_KEY  = "sk_84161a18928796433ed876612ecf52581696accc73c3ce9c";
    private static final String VOICE_ID = "EXAVITQu4vr4xnSDxMaL"; // Bella
    private static final String TTS_URL  = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID;
    private static final String OUTPUT_MP3 = "aura_response.mp3";

    private final GeminiService gemini = new GeminiService();

    public boolean envoyerEtRepondre(String transcription, String moodLabel) {
        System.out.println("[ElevenLabs] Génération réponse AURA...");
        String reponseTexte = gemini.genererReponse(transcription, moodLabel);
        System.out.println("[ElevenLabs] Réponse : " + reponseTexte);

        byte[] audio = appelTTSAPI(reponseTexte, moodLabel);
        if (audio == null || audio.length == 0) {
            System.err.println("[ElevenLabs] Réponse audio vide");
            return false;
        }
        try {
            Files.write(Paths.get(OUTPUT_MP3), audio);
            jouerAudio(OUTPUT_MP3);
            return true;
        } catch (Exception e) {
            System.err.println("[ElevenLabs] Erreur sauvegarde audio : " + e.getMessage());
            return false;
        }
    }

    private byte[] appelTTSAPI(String texte, String moodLabel) {
        try {
            VoiceSettings s = getVoiceSettings(moodLabel);
            String jsonBody = "{"
                    + "\"text\": " + toJsonString(texte) + ","
                    + "\"model_id\": \"eleven_multilingual_v2\","
                    + "\"voice_settings\": {"
                    + "  \"stability\": "        + String.format("%.2f", s.stability).replace(",", ".") + ","
                    + "  \"similarity_boost\": " + String.format("%.2f", s.similarity).replace(",", ".") + ","
                    + "  \"style\": "            + String.format("%.2f", s.style).replace(",", ".")      + ","
                    + "  \"use_speaker_boost\": true"
                    + "}"
                    + "}";

            URL url = new URL(TTS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("xi-api-key", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "audio/mpeg");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                try (InputStream err = conn.getErrorStream()) {
                    if (err != null) System.err.println("[ElevenLabs] Erreur : " + new String(err.readAllBytes()));
                }
                return null;
            }
            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            System.err.println("[ElevenLabs] Erreur TTS : " + e.getMessage());
            return null;
        }
    }

    private void jouerAudio(String filePath) {
        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                AudioFormat baseFormat    = audioStream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(), 16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(), false
                );
                AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
                Clip clip = AudioSystem.getClip();
                clip.open(decoded);
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl vol = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    vol.setValue(2.0f);
                }
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) latch.countDown();
                });
                clip.start();
                latch.await();
                clip.close();
            } catch (Exception e) {
                System.err.println("[ElevenLabs] Erreur lecture : " + e.getMessage());
            }
        }, "AURA-Voice-Thread").start();
    }

    private VoiceSettings getVoiceSettings(String moodLabel) {
        if (moodLabel == null) return new VoiceSettings(0.5f, 0.8f, 0.3f);
        return switch (moodLabel.toLowerCase().trim()) {
            case "stressé", "stresse"   -> new VoiceSettings(0.85f, 0.75f, 0.1f);
            case "fatigué", "fatigue"   -> new VoiceSettings(0.80f, 0.70f, 0.1f);
            case "calme"                -> new VoiceSettings(0.60f, 0.80f, 0.3f);
            case "énergisé", "energise" -> new VoiceSettings(0.40f, 0.85f, 0.7f);
            case "focalisé", "focalise" -> new VoiceSettings(0.70f, 0.80f, 0.2f);
            default                     -> new VoiceSettings(0.55f, 0.80f, 0.3f);
        };
    }

    private record VoiceSettings(float stability, float similarity, float style) {}

    private String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "\"";
    }
}
