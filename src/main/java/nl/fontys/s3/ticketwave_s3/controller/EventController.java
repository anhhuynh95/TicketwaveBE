package nl.fontys.s3.ticketwave_s3.controller;

import nl.fontys.s3.ticketwave_s3.controller.dtos.EventDTO;
import nl.fontys.s3.ticketwave_s3.interfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    @GetMapping
    public List<EventDTO> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return events.stream().map(eventMapper::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EventDTO getEvent(@PathVariable Integer id) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        return eventMapper.toDTO(event);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO);
        eventService.createEvent(event);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEvent(@PathVariable Integer id, @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO);
        event.setId(id);
        eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
    }
}
