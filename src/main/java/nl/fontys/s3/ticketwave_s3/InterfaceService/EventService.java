package nl.fontys.s3.ticketwave_s3.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Domain.Event;

import java.util.List;

public interface EventService {
    List<Event> getAllEvents();
    Event getEventById(Integer id);
    void createEvent(Event event);
    void updateEvent(Integer id, Event event);
    void deleteEvent(Integer id);
}
