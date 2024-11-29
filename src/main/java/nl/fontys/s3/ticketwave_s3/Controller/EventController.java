package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.validation.Valid;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Service.CloudinaryService;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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
    public Page<EventDTO> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventService.getAllEvents(pageable)
                .map(event -> {
                    List<Ticket> tickets = ticketService.getTicketsByEventId(event.getId());
                    EventDTO dto = eventMapper.toDTO(event, tickets);
                    dto.setImageUrl(cloudinaryService.generateImageUrl(String.valueOf(event.getId())));
                    return dto;
                });
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
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@Valid @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toDomain(eventDTO);
        eventService.createEvent(event);
    }

    /**Update an existing event.*/
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEvent(@PathVariable Integer id, @RequestBody EventDTO eventDTO) {
        Event event = eventMapper.toDomain(eventDTO);
        event.setId(id); // Ensure the correct ID is set
        eventService.updateEvent(id, event);
    }

    /**Delete an event by its ID.*/
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Integer id) {
        try {
            eventService.deleteEvent(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** Upload images to Cloudinary */
    @PostMapping("/{eventId}/uploadImage")
    @PreAuthorize("hasRole('MANAGER')")
    public String uploadEventImage(
            @PathVariable String eventId,
            @RequestParam("image") MultipartFile imageFile) throws IOException {

        // Validate file content and type
        if (imageFile.isEmpty() || !isValidImage(imageFile)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        // Create a dedicated temporary directory
        Path tempDir = cloudinaryService.createSecureTempDirectory();

        Path tempFilePath;

        // Create a secure temporary file within the dedicated directory
        if (SystemUtils.IS_OS_UNIX) {
            // For Unix-like systems, set restrictive permissions
            FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rw-------")
            );
            tempFilePath = Files.createTempFile(tempDir, "temp-", imageFile.getOriginalFilename(), attrs);
        } else {
            // For non-Unix systems, set explicit permissions
            tempFilePath = Files.createTempFile(tempDir, "temp-", imageFile.getOriginalFilename());
            File tempFile = tempFilePath.toFile();
            boolean readable = tempFile.setReadable(true, true);
            boolean writable = tempFile.setWritable(true, true);
            boolean executable = tempFile.setExecutable(false, true);

            if (!readable || !writable || executable) {
                throw new IOException("Failed to set secure permissions on temporary file.");
            }
        }

        try {
            // Save the uploaded file securely to the temporary location
            Files.copy(imageFile.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Upload to Cloudinary and return the image URL
            return cloudinaryService.uploadEventImage(tempFilePath.toFile(), eventId);

        } finally {
            // Clean up: delete the temporary file
            Files.deleteIfExists(tempFilePath);
            Files.deleteIfExists(tempDir); // Optionally delete the directory if empty
        }
    }

     /** Validate image file content type */
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /** Search events by query */
    @GetMapping("/search")
    public Page<EventDTO> searchEvents(@RequestParam(required = false) String query,
                                       @RequestParam(required = false) EventType eventType,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Convert empty query to null for backend handling
        String searchQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();

        return (eventType == null
                ? eventService.searchEvents(searchQuery, pageable)
                : eventService.searchEvents(searchQuery, eventType, pageable))
                .map(event -> {
                    List<Ticket> tickets = ticketService.getTicketsByEventId(event.getId());
                    EventDTO dto = eventMapper.toDTO(event, tickets);
                    dto.setImageUrl(cloudinaryService.generateImageUrl(String.valueOf(event.getId())));
                    return dto;
                });
    }
}
