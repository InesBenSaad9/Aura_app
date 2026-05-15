package tn.esprit.aura.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Service Google Calendar pour AURA.
 *
 * SETUP REQUIS :
 *  1. Aller sur https://console.cloud.google.com
 *  2. Créer un projet → Activer "Google Calendar API"
 *  3. Créer des identifiants OAuth 2.0 (type : Desktop App)
 *  4. Télécharger credentials.json → placer dans src/main/resources/
 */
public class GoogleCalendarService {

    // ─── Config ───────────────────────────────────────────────────────────────
    private static final String         APPLICATION_NAME = "AURA - AI Life Companion";
    private static final JsonFactory    JSON_FACTORY     = GsonFactory.getDefaultInstance();
    private static final String         TOKENS_DIR       = "tokens";
    private static final List<String>   SCOPES           = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String         CREDENTIALS_FILE = "/credentials.json";

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static GoogleCalendarService instance;
    private Calendar calendarService;
    private boolean  initialized = false;

    private GoogleCalendarService() {}

    public static GoogleCalendarService getInstance() {
        if (instance == null) instance = new GoogleCalendarService();
        return instance;
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    public boolean init() {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            calendarService = new Calendar.Builder(transport, JSON_FACTORY, getCredentials(transport))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            initialized = true;
            System.out.println("✅ Google Calendar initialisé");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Google Calendar init échoué : " + e.getMessage());
            initialized = false;
            return false;
        }
    }

    private Credential getCredentials(NetHttpTransport transport) throws Exception {
        InputStream in =
                GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE);
        if (in == null) {
            throw new FileNotFoundException("credentials.json introuvable dans resources/");
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIR)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // ─── API Publique ─────────────────────────────────────────────────────────

    /**
     * Ajoute un RDV dans Google Calendar.
     *
     * @param title       Titre de l'événement
     * @param description Description (docteur, motif...)
     * @param startDt     Date/heure de début
     * @param durationMin Durée en minutes
     * @param timeZone    Ex: "Africa/Tunis"
     * @return ID de l'événement créé, ou null si erreur
     */
    public String addEvent(String title, String description,
                           LocalDateTime startDt, int durationMin,
                           String timeZone) {
        if (!initialized) {
            System.err.println("⚠ Google Calendar non initialisé");
            return null;
        }

        try {
            LocalDateTime endDt = startDt.plusMinutes(durationMin);
            ZoneId zone = ZoneId.of(timeZone);

            EventDateTime start = new EventDateTime()
                    .setDateTime(toGoogleDateTime(startDt, zone))
                    .setTimeZone(timeZone);

            EventDateTime end = new EventDateTime()
                    .setDateTime(toGoogleDateTime(endDt, zone))
                    .setTimeZone(timeZone);

            // Rappels : 24h avant + 30min avant
            EventReminder[] reminders = {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(30)
            };

            Event event = new Event()
                    .setSummary(title)
                    .setDescription(description)
                    .setStart(start)
                    .setEnd(end)
                    .setReminders(new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(List.of(reminders)));

            Event created = calendarService.events()
                    .insert("primary", event)
                    .execute();

            System.out.println("✅ Événement créé : " + created.getHtmlLink());
            return created.getId();

        } catch (Exception e) {
            System.err.println("❌ Erreur création événement : " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un événement du calendrier.
     */
    public boolean deleteEvent(String eventId) {
        if (!initialized || eventId == null) return false;
        try {
            calendarService.events().delete("primary", eventId).execute();
            System.out.println("✅ Événement supprimé : " + eventId);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un créneau est disponible pour le docteur.
     *
     * @param startDt Date/heure début
     * @param endDt   Date/heure fin
     * @return true si disponible
     */
    public boolean isSlotAvailable(LocalDateTime startDt, LocalDateTime endDt, String timeZone) {
        if (!initialized) return true; // si pas de calendrier, on laisse passer
        try {
            ZoneId zone = ZoneId.of(timeZone);
            Events events = calendarService.events().list("primary")
                    .setTimeMin(toGoogleDateTime(startDt, zone))
                    .setTimeMax(toGoogleDateTime(endDt, zone))
                    .setSingleEvents(true)
                    .execute();

            boolean available = events.getItems().isEmpty();
            System.out.println("📅 Créneau " + startDt + " → " + (available ? "disponible" : "occupé"));
            return available;

        } catch (Exception e) {
            System.err.println("❌ Erreur vérification créneau : " + e.getMessage());
            return true;
        }
    }

    // ─── Méthodes utilitaires pour l'app ──────────────────────────────────────

    /**
     * Appelé quand un docteur ACCEPTE un RDV patient.
     * Crée l'événement dans les 2 calendriers (même compte partagé ici).
     */
    public String onAppointmentAccepted(String patientName, String doctorName,
                                        LocalDateTime dateTime, String timeZone) {
        String title       = "🩺 Séance AURA — " + doctorName;
        String description = "Patient : " + patientName + "\n" +
                "Docteur : Dr. " + doctorName + "\n" +
                "Application : AURA - AI Life Companion";

        String eventId = addEvent(title, description, dateTime, 60, timeZone);

        if (eventId != null) {
            NotificationService.success("RDV ajouté à votre Google Calendar !");
        } else {
            NotificationService.error("Impossible d'ajouter au Google Calendar.");
        }

        return eventId;
    }

    /**
     * Vérifie disponibilité avant d'accepter.
     */
    public boolean checkDoctorAvailability(LocalDateTime start, String timeZone) {
        LocalDateTime end = start.plusHours(1);
        return isSlotAvailable(start, end, timeZone);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private DateTime toGoogleDateTime(LocalDateTime ldt, ZoneId zone) {
        ZonedDateTime zdt = ldt.atZone(zone);
        return new DateTime(zdt.toInstant().toEpochMilli());
    }

    public boolean isInitialized() { return initialized; }
}
