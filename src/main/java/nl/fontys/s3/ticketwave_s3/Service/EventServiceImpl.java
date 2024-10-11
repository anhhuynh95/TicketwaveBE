package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    /**Fetch all events from the repository.*/
    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**Fetch a specific event by its ID.*/
    @Override
    public Event getEventById(Integer id) {
        return eventRepository.findById(id);
    }

    /**Create a new event and save it to the repository.*/
    @Override
    public void createEvent(Event event) {
        eventRepository.save(event);
    }

    /**Update an existing event by ID.*/
    @Override
    public void updateEvent(Integer id, Event event) {
        event.setId(id); // Ensure the event ID is set before saving
        eventRepository.save(event);
    }

    /**Delete an event by its ID.*/
    @Override
    public void deleteEvent(Integer id) {
        Event event = eventRepository.findById(id);
        if (event == null) {
            // If the event is not found, throw a 404 error
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        eventRepository.deleteById(id); // Proceed to delete the event
    }
}
