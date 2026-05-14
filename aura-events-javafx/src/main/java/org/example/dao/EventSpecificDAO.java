package org.example.dao;

import org.example.aura.entities.Event;
import java.time.LocalDate;
import java.util.List;

public interface EventSpecificDAO {
    List<Event> findByMood(String recommendedMood);
    List<Event> findByType(String type);
    List<Event> findByCreator(int userId);
    List<Event> findByDateRange(LocalDate start, LocalDate end);
    List<Event> findUpcomingEvents();
    List<Event> findPastEvents();
    List<Event> findByCompatibleMood(String userMood);
    List<Event> findEventsWithAvailableSlots();
    boolean addParticipant(int eventId, int userId);
    boolean removeParticipant(int eventId, int userId);
    List<Integer> getParticipants(int eventId);
    int getCurrentParticipantCount(int eventId);
    boolean canJoin(int eventId);
    int countEventsByCreator(int userId);
    int countEventsByType(String type);
}
