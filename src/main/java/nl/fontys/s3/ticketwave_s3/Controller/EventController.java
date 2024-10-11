package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    /**Retrieve all events.*/
    @GetMapping
    public List<EventDTO> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return events.stream()
                .map(eventMapper::toDTO) // Map Event entities to DTOs
                .collect(Collectors.toList());
    }

    /**Retrieve a specific event by its ID.*/
    @GetMapping("/{id}")
    public EventDTO getEvent(@PathVariable Integer id) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        return eventMapper.toDTO(event); // Map Event entity to DTO
    }

    /**Create a new event.*/
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO); // Map DTO to Event entity
        eventService.createEvent(event);
    }

    /**Update an existing event.*/
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEvent(@PathVariable Integer id, @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO); // Map DTO to Event entity
        event.setId(id); // Ensure the correct ID is set
        eventService.updateEvent(id, event);
    }

    /**Delete an event by its ID.*/
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
    }
}
