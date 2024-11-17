package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.validation.Valid;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/events")
@Validated
public class EventController {

    private final CloudinaryService cloudinaryService;

    private final EventService eventService;

    private final EventMapper eventMapper;

    private final TicketService ticketService;

    public EventController(EventService eventService, EventMapper eventMapper, TicketService ticketService, CloudinaryService cloudinaryService) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.ticketService = ticketService;
        this.cloudinaryService = cloudinaryService;
    }

    /**Retrieve all events.*/
    @GetMapping
    public List<EventDTO> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return events.stream()
                .map(event -> {
                    List<Ticket> tickets = ticketService.getTicketsByEventId(event.getId());
                    EventDTO dto = eventMapper.toDTO(event, tickets);
                    // Dynamically add the Cloudinary image URL
                    dto.setImageUrl(cloudinaryService.generateImageUrl(String.valueOf(event.getId())));
                    return dto;
                })
                .toList();
    }

    /** Get details of a specific event by ID. */
    @GetMapping("/{id}")
    public EventDTO getEvent(@PathVariable Integer id) {
        try {
            Event event = eventService.getEventById(id);
            List<Ticket> tickets = ticketService.getTicketsByEventId(id);
            EventDTO dto = eventMapper.toDTO(event, tickets);
            // Dynamically add the Cloudinary image URL
            dto.setImageUrl(cloudinaryService.generateImageUrl(String.valueOf(id)));
            return dto;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**Create a new event.*/
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@Valid @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toDomain(eventDTO);
        eventService.createEvent(event);
    }

    /**Update an existing event.*/
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEvent(@PathVariable Integer id, @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toDomain(eventDTO);
        event.setId(id); // Ensure the correct ID is set
        eventService.updateEvent(id, event);
    }

    /**Delete an event by its ID.*/
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Integer id) {
        try {
            eventService.deleteEvent(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**Upload images to Cloudinary*/
    @PostMapping("/{eventId}/uploadImage")
    public String uploadEventImage(@PathVariable String eventId, @RequestParam("image") MultipartFile imageFile) throws IOException {
        // Save the uploaded file to a temporary location
        File file = File.createTempFile("temp", imageFile.getOriginalFilename());
        imageFile.transferTo(file);

        // Upload to Cloudinary and get the image URL
        String imageUrl = cloudinaryService.uploadEventImage(file, eventId);

        // Clean up temporary file
        file.delete();

        // Return the URL to the uploaded image
        return imageUrl;
    }
}
