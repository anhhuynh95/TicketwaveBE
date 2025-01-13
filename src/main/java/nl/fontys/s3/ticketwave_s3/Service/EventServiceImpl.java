package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final GeocodingService geocodingService;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper, GeocodingService geocodingService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.geocodingService = geocodingService;
    }

    /** Retrieve all events. */
    @Override
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
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
    public Event createEvent(Event event) {
        double[] coordinates = geocodingService.getCoordinates(event.getLocation());
        event.setLatitude(coordinates[0]);
        event.setLongitude(coordinates[1]);
        return eventRepository.save(event);
    }

    /** Update an existing event by ID. */
    @Override
    public void updateEvent(Integer id, Event event) {
        event.setId(id);
        double[] coordinates = geocodingService.getCoordinates(event.getLocation());
        event.setLatitude(coordinates[0]);
        event.setLongitude(coordinates[1]);
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

    @Override
    public Page<Event> searchEvents(String query, EventType eventType, Double latitude, Double longitude, Double radius, Pageable pageable) {
        String searchQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();

        // Log search parameters for debugging
        System.out.println("Query: " + searchQuery);
        System.out.println("EventType: " + eventType);
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("Radius: " + radius);

        // Fetch events using the repository
        Page<EventEntity> results = eventRepository.searchEvents(searchQuery, eventType, latitude, longitude, radius, pageable);

        // Log the results for debugging
        System.out.println("Filtered Results: " + results.getContent().size());
        results.forEach(event -> System.out.println("Event: " + event.getName() + " at " + event.getLocation()));

        // Map entities to domain objects
        return results.map(eventMapper::toDomain);
    }


}
