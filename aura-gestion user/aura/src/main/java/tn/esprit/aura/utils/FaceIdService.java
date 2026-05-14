package tn.esprit.aura.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceIdService {

    // No API key needed for local server
    private static final String MODEL_URL = "http://localhost:5050/api/embeddings";

    /**
     * Get feature embeddings for an image file using the Local Python API.
     */
    public static double[] getEmbeddings(byte[] imageBytes) throws Exception {
        URL url = new URL(MODEL_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(imageBytes);
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Erreur API : " + conn.getResponseCode() + " " + conn.getResponseMessage());
        }

        Scanner scanner = new Scanner(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        double[] embeddings = parseNumbers(response.toString());
        if (embeddings.length == 0) {
            throw new RuntimeException("Impossible de parser les embeddings depuis la reponse : " + response);
        }
        return embeddings;
    }

    /**
     * Calculate Cosine Similarity between two embedding vectors.
     */
    public static double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Convert embedding array to String for database storage.
     */
    public static String embeddingsToString(double[] embeddings) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < embeddings.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(embeddings[i]);
        }
        json.append(']');
        return json.toString();
    }

    /**
     * Parse embedding String from database back to array.
     */
    public static double[] stringToEmbeddings(String data) {
        if (data == null || data.isEmpty()) return null;
        return parseNumbers(data);
    }

    private static double[] parseNumbers(String jsonText) {
        Pattern numberPattern = Pattern.compile("-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
        Matcher matcher = numberPattern.matcher(jsonText);
        List<Double> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group()));
        }

        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
