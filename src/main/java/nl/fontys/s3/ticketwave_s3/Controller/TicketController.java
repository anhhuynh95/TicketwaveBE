package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private static final String INVALID_ID_MESSAGE = "ID must be a positive integer.";
    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found.";

    private final TicketService ticketService;

    private final UserService userService;

    private final TicketMapper ticketMapper;

    private final EventService eventService;

    public TicketController(TicketService ticketService, UserService userService, TicketMapper ticketMapper, EventService eventService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.ticketMapper = ticketMapper;
        this.eventService = eventService;
    }

    /**Retrieve all tickets, optionally filtered by max price.*/
    @GetMapping()
    public List<TicketDTO> getAllTickets(@RequestParam(required = false) Double maxPrice) {
        List<Ticket> tickets = (maxPrice != null)
                ? ticketService.getTicketsByPrice(maxPrice)
                : ticketService.getAllTickets();

        if (tickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tickets available.");
        }

        return tickets.stream()
                .map(ticketMapper::toDTO)
                .toList();
    }

    /**Retrieve a specific ticket by ID.*/
    @GetMapping("/{id}")
    public TicketDTO getTicket(@PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE);
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }

        return ticketMapper.toDTO(ticket);
    }

    /**Create a new ticket associated with an event.**/
    @PostMapping("/create")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@RequestParam Integer eventId, @RequestBody TicketDTO input) {
        if (input.getQuantity() == null || input.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be a positive integer.");
        }

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        List<Ticket> existingTickets = ticketService.getTicketsByEventId(eventId);
        int usedQuantity = existingTickets.stream().mapToInt(Ticket::getQuantity).sum();

        if (usedQuantity + input.getQuantity() > event.getTicketQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exceeds available ticket quantity.");
        }

        Ticket ticket = ticketMapper.toDomain(input);
        ticket.setEventId(eventId);
        ticketService.createTicket(ticket);
    }

    /**Update an existing ticket.**/
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTicket(@PathVariable Integer id, @RequestBody TicketDTO input) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE);
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }

        Ticket updatedTicket = ticketMapper.toDomain(input);
        updatedTicket.setId(id);
        ticketService.updateTicket(id, updatedTicket);
    }

    /**Delete a specific ticket by ID.**/
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(@PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE);
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }

        ticketService.deleteTicket(id);
    }

    /** Get all tickets for a specific event. */
    @GetMapping("/by-event/{eventId}")
    public List<TicketDTO> getTicketsByEventId(@PathVariable Integer eventId) {
        List<Ticket> tickets = ticketService.getTicketsByEventId(eventId);
        return tickets.stream()
                .map(ticketMapper::toDTO)
                .toList();
    }

    /** Purchase a ticket by ticket ID and quantity. */
    @PutMapping("/{eventId}/{ticketId}/purchase")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public void purchaseTicket(@PathVariable Integer eventId, @PathVariable Integer ticketId, @RequestParam Integer quantity, Authentication authentication) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be a positive integer.");
        }
        String email = authentication.getName(); // Extract user's email from authentication
        Integer userId = userService.findUserIdByEmail(email); // Fetch userId from the database

        ticketService.purchaseTicket(ticketId, quantity, userId);
    }

    /** Get all purchased tickets. */
    @GetMapping("/purchased")
    @PreAuthorize("hasRole('USER')")
    public List<PurchasedTicketDTO> getPurchasedTickets(Authentication authentication) {
        String email = authentication.getName(); // Extract the user's email
        Integer userId = userService.findUserIdByEmail(email);
        return ticketService.getPurchasedTickets(userId);
    }

    /** Cancel a specific quantity of purchased tickets. */
    @PutMapping("/{ticketId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelTicket(@PathVariable Integer ticketId, @RequestBody Map<String, Integer> requestBody) {
       Integer cancelQuantity = requestBody.get("cancelQuantity");
        if (cancelQuantity == null || cancelQuantity <= 0) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cancel quantity.");
        }
       ticketService.cancelTickets(ticketId, cancelQuantity);
       return ResponseEntity.noContent().build();
    }

//    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/analytics/event-type-sales")
    public Map<String, Long> getEventTypeSales() {
        System.out.println("Inside getEventTypeSales");
        return ticketService.getTotalPurchasesByEventType();
    }

//    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/analytics/monthly-sales")
    public Map<String, Double> getMonthlySales() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return ticketService.getMonthlySales(sixMonthsAgo);
    }

}
