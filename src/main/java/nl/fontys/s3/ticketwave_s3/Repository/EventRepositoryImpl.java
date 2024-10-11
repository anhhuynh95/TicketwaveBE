package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepository {
    // In-memory list of events acting as the data store
    private final List<Event> events = new ArrayList<>();
    private int nextId = 1; // Counter for generating unique event IDs

    // Constructor to initialize some sample events
    public EventRepositoryImpl() {
        events.add(new Event(nextId++, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 100));
        events.add(new Event(nextId++, "Art Exhibition", "Nuenen", "A stunning art exhibition", "2024-09-05T18:00", 50));
        events.add(new Event(nextId++, "Sports Event", "Amsterdam", "An amazing football match", "2024-09-10T21:00", 200));
    }

    /**Retrieve all events from the in-memory store*/
    @Override
    public List<Event> findAll() {
        // Return a new list to protect the original list from modification
        return new ArrayList<>(events);
    }

    /**Find an event by its ID.*/
    @Override
    public Event findById(Integer id) {
        // Iterate through the events and return the event with matching ID
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null; // Return null if no event is found
    }

    /**Save or update an event in the repository.*/
    @Override
    public void save(Event event) {
        if (event.getId() == null) {
            // If the event is new (no ID), assign a new ID and add it to the list
            event.setId(nextId++);
            events.add(event);
        } else {
            // If the event already exists, update the existing event's details
            Event existingEvent = findById(event.getId());
            if (existingEvent != null) {
                existingEvent.setName(event.getName());
                existingEvent.setLocation(event.getLocation());
                existingEvent.setDescription(event.getDescription());
                existingEvent.setDateTime(event.getDateTime());
                existingEvent.setTicketQuantity(event.getTicketQuantity());
            }
        }
    }

    /**Delete an event by its ID.*/
    @Override
    public void deleteById(Integer id) {
        // Find and remove the event from the list
        Event event = findById(id);
        if (event != null) {
            events.remove(event);
        }
    }
}
