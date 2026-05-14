package tn.esprit.aura.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private int idEvent;
    private String title;
    private String description;
    private LocalDate eventDate;
    private String eventTime;
    private String location;
    private int maxParticipants;
    private String recommendedMood;
    private int createdBy;
    private List<Integer> participants;
    private String imageUrl;
    private double price;
    private String paymentMethod;
    private int discountPercent;

    public Event() {
        this.participants = new ArrayList<>();
        this.maxParticipants = 50;
        this.recommendedMood = "neutre";
        this.price = 0.0;
        this.paymentMethod = "Gratuit";
        this.discountPercent = 0;
    }

    public Event(int idEvent, String title, String description,
                 LocalDate eventDate, String eventTime, String location) {
        this.idEvent = idEvent;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.participants = new ArrayList<>();
        this.maxParticipants = 50;
        this.recommendedMood = "neutre";
    }

    public int getIdEvent() { return idEvent; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }
    public String getLocation() { return location; }
    public int getMaxParticipants() { return maxParticipants; }
    public String getRecommendedMood() { return recommendedMood; }
    public int getCreatedBy() { return createdBy; }
    public List<Integer> getParticipants() { return participants; }
    public double getPrice() { return price; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getDiscountPercent() { return discountPercent; }

    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public void setLocation(String location) { this.location = location; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setRecommendedMood(String recommendedMood) { this.recommendedMood = recommendedMood; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public void setParticipants(List<Integer> participants) { this.participants = participants; }
    public void setPrice(double price) { this.price = Math.max(0.0, price); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = Math.max(0, Math.min(100, discountPercent));
    }

    public boolean isPaidEvent() { return price > 0.0; }
    public double getDiscountedPrice() {
        return price <= 0 ? 0.0 : price * (100 - discountPercent) / 100.0;
    }

    public String getFullDateTime() { return eventDate + " à " + eventTime; }
    public int getCurrentParticipantsCount() { return participants.size(); }
    public boolean hasAvailableSlots() { return participants.size() < maxParticipants; }
    public boolean isParticipant(int userId) { return participants.contains(userId); }

    public boolean addParticipant(int userId) {
        if (hasAvailableSlots() && !isParticipant(userId)) {
            return participants.add(userId);
        }
        return false;
    }

    public boolean removeParticipant(int userId) {
        return participants.remove(Integer.valueOf(userId));
    }

    @Override
    public String toString() {
        return String.format("Event{id=%d, title='%s', date=%s, time=%s, location='%s', participants=%d/%d}",
                idEvent, title, eventDate, eventTime, location, getCurrentParticipantsCount(), maxParticipants);
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
