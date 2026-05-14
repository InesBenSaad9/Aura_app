package org.example.services;

import org.example.dao.EventDAO;
import org.example.aura.entities.Event;
import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
public class EventService {

    private final EventDAO eventDAO;

    public enum ParticipationStatus {
        ACCEPTED,
        REFUSED_FULL,
        REFUSED_PAST,
        ALREADY_REGISTERED,
        EVENT_NOT_FOUND
    }
    public record ParticipationTicket(
            int eventId,
            int userId,
            String eventTitle,
            String ticketCode,
            String confirmationText,
            String gmailUrl,
            String qrCodeUrl
    ) {
    }

    public record EventAvailability(
            int eventId,
            int capacity,
            int confirmedParticipants,
            int availableSpots,
            boolean available,
            boolean full
    ) {
    }

    public record ParticipationDecision(
            ParticipationStatus status,
            boolean accepted,
            String message,
            EventAvailability availability
    ) {
    }

    public record ParticipantRequest(
            int userId,
            String status,
            String role,
            String registrationDate
    ) {
    }

    public record EventFeedback(
            int userId,
            int rating,
            String comment,
            String createdAt
    ) {
    }

    public record FeedbackStats(
            int totalFeedbacks,
            double averageRating,
            int positiveCount,
            int neutralCount,
            int negativeCount
    ) {
    }

    public record EventOrganizer(
            int idOrganizer,
            String fullName,
            String email,
            String phone,
            String bio,
            boolean active
    ) {
    }


    public EventService() {
        this.eventDAO = new EventDAO();
    }

    public Event createEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire");
        if (event.getEventDate() == null || event.getEventDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("La date doit être dans le futur");
        return eventDAO.insert(event);
    }

    public Optional<Event> getEventById(int id) { return eventDAO.findById(id); }
    public List<Event> getAllEvents()            { return eventDAO.findAll(); }
    public List<Event> getUpcomingEvents()       { return eventDAO.findUpcomingEvents(); }
    public List<Event> getPastEvents()           { return eventDAO.findPastEvents(); }

    public List<Event> getEventsByDateRange(LocalDate start, LocalDate end) {
        return eventDAO.findByDateRange(start, end);
    }

    public List<Event> getEventsByMood(String mood) {
        if (mood == null || mood.isBlank() || mood.equalsIgnoreCase("all")) return eventDAO.findUpcomingEvents();
        return eventDAO.findByMood(mood);
    }

    public List<Event> getMyEvents(int userId) { return eventDAO.findByCreator(userId); }

    public Event updateEvent(Event event) {
        if (!eventDAO.exists(event.getIdEvent()))
            throw new RuntimeException("L'événement n'existe pas");
        return eventDAO.update(event);
    }

    public boolean deleteEvent(int id)              { return eventDAO.deleteById(id); }
    public boolean joinEvent(int eventId, int userId)  {
        return requestParticipation(eventId, userId).accepted();
    }
    public boolean leaveEvent(int eventId, int userId) { return eventDAO.removeParticipant(eventId, userId); }

    public List<ParticipantRequest> getParticipantRequests(int eventId) {
        return eventDAO.getParticipationRows(eventId).stream()
                .map(row -> new ParticipantRequest(
                        row.userId(),
                        normalizeParticipationStatus(row.status()),
                        row.role(),
                        row.registrationDate()
                ))
                .toList();
    }

    public boolean acceptParticipant(int eventId, int userId) {
        EventAvailability availability = getEventAvailability(eventId);
        boolean alreadyConfirmed = getParticipantRequests(eventId).stream()
                .anyMatch(p -> p.userId() == userId && isConfirmedStatus(p.status()));
        if (!alreadyConfirmed && !availability.available()) {
            return false;
        }
        return eventDAO.updateParticipantStatus(eventId, userId, "accepted");
    }

    public boolean rejectParticipant(int eventId, int userId) {
        return eventDAO.updateParticipantStatus(eventId, userId, "rejected");
    }

    public boolean addFeedback(int eventId, int userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit etre entre 1 et 5.");
        }
        if (!eventDAO.exists(eventId)) {
            throw new IllegalArgumentException("Evenement introuvable.");
        }
        return eventDAO.saveFeedback(eventId, userId, rating, comment == null ? "" : comment.trim());
    }

    public List<EventFeedback> getFeedbacks(int eventId) {
        return eventDAO.getFeedbackRows(eventId).stream()
                .map(row -> new EventFeedback(
                        row.userId(),
                        row.rating(),
                        row.comment(),
                        row.createdAt()
                ))
                .toList();
    }

    public FeedbackStats getFeedbackStats(int eventId) {
        List<EventFeedback> feedbacks = getFeedbacks(eventId);
        if (feedbacks.isEmpty()) {
            return new FeedbackStats(0, 0, 0, 0, 0);
        }

        int total = feedbacks.size();
        int sum = feedbacks.stream().mapToInt(EventFeedback::rating).sum();
        int positive = (int) feedbacks.stream().filter(f -> f.rating() >= 4).count();
        int neutral = (int) feedbacks.stream().filter(f -> f.rating() == 3).count();
        int negative = (int) feedbacks.stream().filter(f -> f.rating() <= 2).count();
        return new FeedbackStats(total, (double) sum / total, positive, neutral, negative);
    }

    public Optional<EventOrganizer> getOrganizerById(int organizerId) {
        return eventDAO.findOrganizerById(organizerId)
                .map(row -> new EventOrganizer(
                        row.idOrganizer(),
                        row.fullName(),
                        row.email(),
                        row.phone(),
                        row.bio(),
                        row.active()
                ));
    }

    public int getAvailableSpots(int eventId) {
        return getEventAvailability(eventId).availableSpots();
    }

    public int getCurrentParticipantCount(int eventId) {
        return eventDAO.getCurrentParticipantCount(eventId);
    }

    public int getRejectedParticipantCount(int eventId) {
        return (int) getParticipantRequests(eventId).stream()
                .filter(p -> isRejectedStatus(p.status()))
                .count();
    }

    public EventAvailability getEventAvailability(int eventId) {
        Optional<Event> opt = eventDAO.findById(eventId);
        if (opt.isEmpty()) {
            return new EventAvailability(eventId, 0, 0, 0, false, true);
        }

        Event event = opt.get();
        int capacity = Math.max(0, event.getMaxParticipants());
        int count = eventDAO.getCurrentParticipantCount(eventId);
        int availableSpots = Math.max(0, capacity - count);
        boolean upcoming = event.getEventDate() == null || !event.getEventDate().isBefore(LocalDate.now());
        boolean available = upcoming && availableSpots > 0;
        return new EventAvailability(eventId, capacity, count, availableSpots, available, availableSpots <= 0);
    }

    public ParticipationDecision requestParticipation(int eventId, int userId) {
        Optional<Event> opt = eventDAO.findById(eventId);
        if (opt.isEmpty()) {
            EventAvailability availability = new EventAvailability(eventId, 0, 0, 0, false, true);
            return new ParticipationDecision(
                    ParticipationStatus.EVENT_NOT_FOUND,
                    false,
                    "Evenement introuvable.",
                    availability
            );
        }

        Event event = opt.get();
        Optional<EventDAO.ParticipationRow> existingParticipation = eventDAO.findParticipationRow(eventId, userId);
        if (existingParticipation.isPresent() && isConfirmedStatus(existingParticipation.get().status())) {
            return new ParticipationDecision(
                    ParticipationStatus.ALREADY_REGISTERED,
                    true,
                    "Vous etes deja inscrit a cet evenement.",
                    getEventAvailability(eventId)
            );
        }

        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDate.now())) {
            return new ParticipationDecision(
                    ParticipationStatus.REFUSED_PAST,
                    false,
                    "Inscription refusee : cet evenement est deja passe.",
                    getEventAvailability(eventId)
            );
        }

        EventAvailability beforeJoin = getEventAvailability(eventId);
        if (!beforeJoin.available()) {
            return new ParticipationDecision(
                    ParticipationStatus.REFUSED_FULL,
                    false,
                    "Inscription refusee : les places sont insuffisantes.",
                    beforeJoin
            );
        }

        if (existingParticipation.isPresent()) {
            boolean reactivated = eventDAO.updateParticipantStatus(eventId, userId, "accepted");
            EventAvailability afterReactivate = getEventAvailability(eventId);
            if (reactivated) {
                return new ParticipationDecision(
                        ParticipationStatus.ACCEPTED,
                        true,
                        "Votre inscription a ete confirmee.",
                        afterReactivate
                );
            }
        }

        boolean inserted = eventDAO.addParticipant(eventId, userId);
        EventAvailability afterJoin = getEventAvailability(eventId);
        if (!inserted) {
            return new ParticipationDecision(
                    afterJoin.full() ? ParticipationStatus.REFUSED_FULL : ParticipationStatus.EVENT_NOT_FOUND,
                    false,
                    afterJoin.full()
                            ? "Inscription refusee : les places sont insuffisantes."
                            : "Inscription impossible pour cet evenement.",
                    afterJoin
            );
        }

        return new ParticipationDecision(
                ParticipationStatus.ACCEPTED,
                true,
                "Inscription acceptee. Votre place est confirmee.",
                afterJoin
        );
    }

    public ParticipationTicket createParticipationTicket(int eventId, int userId) {
        Optional<Event> opt = eventDAO.findById(eventId);
        if (opt.isEmpty() || !opt.get().isParticipant(userId)) {
            throw new IllegalArgumentException("Aucune inscription acceptee pour generer la confirmation.");
        }

        Event event = opt.get();
        String ticketCode = "AURA-E" + eventId + "-U" + userId;
        String confirmationText = "Inscription acceptee\n"
                + "Ticket: " + ticketCode + "\n"
                + "Evenement: " + event.getTitle() + "\n"
                + "Date: " + (event.getEventDate() == null ? "-" : event.getEventDate()) + "\n"
                + "Heure: " + (event.getEventTime() == null ? "-" : event.getEventTime()) + "\n"
                + "Lieu: " + (event.getLocation() == null ? "-" : event.getLocation()) + "\n"
                + "Participant: utilisateur #" + userId;

        return new ParticipationTicket(
                eventId,
                userId,
                event.getTitle(),
                ticketCode,
                confirmationText,
                buildGmailConfirmationUrl(event.getTitle(), confirmationText),
                buildQrCodeUrl(confirmationText)
        );
    }

    public String buildGmailConfirmationUrl(String eventTitle, String confirmationText) {
        String subject = "Confirmation Aura - " + eventTitle;
        return "https://mail.google.com/mail/?view=cm&fs=1"
                + "&su=" + urlEncode(subject)
                + "&body=" + urlEncode(confirmationText);
    }

    public String buildQrCodeUrl(String confirmationText) {
        return "https://quickchart.io/qr?size=220&text=" + urlEncode(confirmationText);
    }

    public String buildGoogleMapsUrl(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        return "https://www.google.com/maps/search/?api=1&query=" + urlEncode(location.trim());
    }

    public boolean canJoin(int eventId) { return eventDAO.canJoin(eventId); }
    public int getTotalEventsCount()    { return eventDAO.count(); }
    public void close()                 { eventDAO.close(); }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private boolean isConfirmedStatus(String status) {
        String value = normalizeStatusText(status);
        if (isNegativeConfirmationStatus(value)) {
            return false;
        }
        return value.contains("confirm")
                || value.contains("accept")
                || value.contains("accepte")
                || value.contains("valid")
                || value.contains("approved")
                || value.contains("approuve");
    }

    private boolean isRejectedStatus(String status) {
        String value = normalizeStatusText(status);
        return value.contains("reject")
                || value.contains("refus")
                || value.contains("rejet")
                || value.contains("declin")
                || value.contains("denied")
                || value.contains("cancel")
                || value.contains("annul")
                || isNegativeConfirmationStatus(value);
    }

    private String normalizeParticipationStatus(String status) {
        if (status == null || status.isBlank()) {
            return "confirmed";
        }
        return status.trim().toLowerCase();
    }

    private String normalizeStatusText(String status) {
        if (status == null) {
            return "";
        }
        return java.text.Normalizer.normalize(status.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private boolean isNegativeConfirmationStatus(String normalized) {
        return normalized.contains("non confirme")
                || normalized.contains("not confirmed")
                || normalized.contains("unconfirmed");
    }
}
