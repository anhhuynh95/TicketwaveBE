package nl.fontys.s3.ticketwave_s3.controller;

import nl.fontys.s3.ticketwave_s3.controller.dtos.TicketDTO;
import nl.fontys.s3.ticketwave_s3.controller.dtos.TicketsDTO;
import nl.fontys.s3.ticketwave_s3.models.Ticket;
import nl.fontys.s3.ticketwave_s3.business.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping()
    public TicketsDTO getAllTickets(@RequestParam(required = false) Double maxPrice) {
        List<Ticket> tickets;
        if (maxPrice != null) {
            tickets = ticketService.getTicketsByPrice(maxPrice);
        } else {
            tickets = ticketService.getAllTickets();
        }

        if (tickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tickets available.");
        }

        TicketsDTO result = new TicketsDTO();
        for (Ticket ticket : tickets) {
            TicketDTO ticketDTO = new TicketDTO();
            ticketDTO.setId(ticket.getId());
            ticketDTO.setEventName(ticket.getEventName());
            ticketDTO.setLocation(ticket.getLocation());
            ticketDTO.setPrice(ticket.getPrice());
            result.getTickets().add(ticketDTO);
        }

        return result;
    }

    @GetMapping("/{id}")
    public TicketDTO getTicket(@PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be a positive integer.");
        }

        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setEventName(ticket.getEventName());
        ticketDTO.setLocation(ticket.getLocation());
        ticketDTO.setPrice(ticket.getPrice());
        return ticketDTO;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@RequestBody TicketDTO input) {
        Ticket ticket = new Ticket(null, input.getEventName(), input.getLocation(), input.getPrice());
        ticketService.createTicket(ticket);
    }

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

        Ticket updatedTicket = new Ticket(null, input.getEventName(), input.getLocation(), input.getPrice());
        ticketService.updateTicket(id, updatedTicket);
    }

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
}

