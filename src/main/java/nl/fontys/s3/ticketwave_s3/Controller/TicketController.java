package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.dtos.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.dtos.TicketsDTO;
import nl.fontys.s3.ticketwave_s3.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
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

    @Autowired
    private TicketMapper ticketMapper;

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
            TicketDTO ticketDTO = ticketMapper.toDTO(ticket); // Use the mapper
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

        return ticketMapper.toDTO(ticket); // Use the mapper
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@RequestBody TicketDTO input) {
        Ticket ticket = ticketMapper.toEntity(input); // Use the mapper
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

        Ticket updatedTicket = ticketMapper.toEntity(input); // Use the mapper
        updatedTicket.setId(id); // Set the ID to the existing ticket
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

