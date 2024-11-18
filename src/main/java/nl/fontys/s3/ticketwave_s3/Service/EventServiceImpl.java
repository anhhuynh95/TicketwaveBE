package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /** Retrieve all events. */
    @Override
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable); // Pass pageable to repository
    }

    /** Find a specific event by ID. */
    @Override
    public Event getEventById(Integer id) {
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        return event;
    }

    /** Save a new event. */
    @Override
    public void createEvent(Event event) {
        eventRepository.save(event);
    }

    /** Update an existing event by ID. */
    @Override
    public void updateEvent(Integer id, Event event) {
        event.setId(id);
        eventRepository.save(event);
    }

    /** Delete an event by ID. */
    @Override
    public void deleteEvent(Integer id) {
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        eventRepository.deleteById(id);
    }

}
