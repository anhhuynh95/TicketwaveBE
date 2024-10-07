package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepository {
    private final List<Event> events = new ArrayList<>();
    private int nextId = 1;

    public EventRepositoryImpl() {
        events.add(new Event(nextId++, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00"));
        events.add(new Event(nextId++, "Art Exhibition", "Nuenen", "A stunning art exhibition", "2024-09-05T18:00"));
        events.add(new Event(nextId++, "Sports Event", "Amsterdam", "An amazing football match", "2024-09-10T21:00"));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(events);
    }

    @Override
    public Event findById(Integer id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    @Override
    public void save(Event event) {
        if (event.getId() == null) {
            event.setId(nextId++);
            events.add(event);
        } else {
            Event existingEvent = findById(event.getId());
            if (existingEvent != null) {
                existingEvent.setName(event.getName());
                existingEvent.setLocation(event.getLocation());
                existingEvent.setDescription(event.getDescription());
                existingEvent.setDateTime(event.getDateTime());
            }
        }
    }

    @Override
    public void deleteById(Integer id) {
        Event event = findById(id);
        if (event != null) {
            events.remove(event);
        }
    }
}
