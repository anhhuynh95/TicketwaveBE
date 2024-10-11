package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private EventService eventService;

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
                .collect(Collectors.toList());
    }

    /**Retrieve a specific ticket by ID.*/
    @GetMapping("/{id}")
    public TicketDTO getTicket(@PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be a positive integer.");
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        return ticketMapper.toDTO(ticket);
    }

    /**Create a new ticket associated with an event.*/
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@RequestParam Integer eventId, @RequestBody TicketDTO input) {
        // Ensure the event exists
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        // Create the ticket and set its event details
        Ticket ticket = ticketMapper.toEntity(input);
        ticket.setEventId(eventId);
        ticket.setQuantity(event.getTicketQuantity());
        ticket.setEventName(event.getName());
        ticket.setLocation(event.getLocation());

        ticketService.createTicket(ticket);
    }

    /**Update an existing ticket.*/
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTicket(@PathVariable Integer id, @RequestBody TicketDTO input) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be a positive integer.");
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        Ticket updatedTicket = ticketMapper.toEntity(input);
        updatedTicket.setId(id);
        ticketService.updateTicket(id, updatedTicket);
    }

    /**Delete a specific ticket by ID.*/
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(@PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be a positive integer.");
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        ticketService.deleteTicket(id);
    }

    /**Purchase a ticket by updating the quantity.*/
    @PutMapping("/{id}/purchase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purchaseTicket(@PathVariable Integer id, @RequestParam Integer quantity) {
        if (id <= 0 || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID and quantity must be positive integers.");
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        ticketService.purchaseTicket(id, quantity);
    }

    /**Retrieve all purchased tickets.*/
    @GetMapping("/purchased")
    public List<TicketDTO> getPurchasedTickets() {
        List<Ticket> purchasedTickets = ticketService.getPurchasedTickets();
        return purchasedTickets.stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**Cancel a specific quantity of tickets.*/
    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTicket(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        int cancelQuantity = body.get("cancelQuantity");

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        if (cancelQuantity <= 0 || cancelQuantity > ticket.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cancel quantity.");
        }

        // Reduce the purchased quantity
        ticket.setQuantity(ticket.getQuantity() - cancelQuantity);
        ticketService.updateTicket(id, ticket);

        // Update the event's available tickets
        Event event = eventService.getEventById(ticket.getEventId());
        event.setTicketQuantity(event.getTicketQuantity() + cancelQuantity);
        eventService.updateEvent(event.getId(), event);
    }
}
