package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Page<Event> getAllEvents(Pageable pageable);
    Event getEventById(Integer id);
    Event createEvent(Event event);
    void updateEvent(Integer id, Event event);
    void deleteEvent(Integer id);
    Page<Event> searchEvents(String query, Pageable pageable);
    Page<Event> searchEvents(String query, EventType eventType, Pageable pageable);
}
