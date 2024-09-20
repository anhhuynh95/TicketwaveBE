package nl.fontys.s3.ticketwave_s3.controller;

import nl.fontys.s3.ticketwave_s3.controller.dtos.TicketDTO;
import nl.fontys.s3.ticketwave_s3.controller.dtos.TicketsDTO;
import nl.fontys.s3.ticketwave_s3.models.Ticket;
import nl.fontys.s3.ticketwave_s3.business.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping()
    public TicketsDTO getAllTickets(@RequestParam(required = false) Double maxPrice) {
        List<Ticket> tickets;

        // If maxPrice is provided, filter tickets by price; otherwise, get all tickets
        if (maxPrice != null) {
            tickets = ticketService.getTicketsByPrice(maxPrice);
        } else {
            tickets = ticketService.getAllTickets();
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
        Ticket ticket = ticketService.getTicketById(id);

        // Convert the domain object to a DTO
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
}

