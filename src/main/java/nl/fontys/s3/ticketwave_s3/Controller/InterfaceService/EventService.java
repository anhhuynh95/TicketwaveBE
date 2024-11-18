package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Page<Event> getAllEvents(Pageable pageable);
    Event getEventById(Integer id);
    void createEvent(Event event);
    void updateEvent(Integer id, Event event);
    void deleteEvent(Integer id);

}
