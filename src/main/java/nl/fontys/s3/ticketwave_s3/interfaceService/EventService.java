package nl.fontys.s3.ticketwave_s3.interfaceService;

import nl.fontys.s3.ticketwave_s3.models.Event;

import java.util.List;

public interface EventService {
    List<Event> getAllEvents();
    Event getEventById(Integer id);
    void createEvent(Event event);
    void updateEvent(Integer id, Event event);
    void deleteEvent(Integer id);
}
