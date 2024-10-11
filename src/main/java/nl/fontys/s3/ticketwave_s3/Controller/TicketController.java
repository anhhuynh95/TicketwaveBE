package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMapper ticketMapper;

    @GetMapping()
    public List<TicketDTO> getAllTickets(@RequestParam(required = false) Double maxPrice) {
        List<Ticket> tickets;
        if (maxPrice != null) {
            tickets = ticketService.getTicketsByPrice(maxPrice);
        } else {
            tickets = ticketService.getAllTickets();
        }

        if (tickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tickets available.");
        }

        return tickets.stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
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

        return ticketMapper.toDTO(ticket);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public void createTicket(@RequestBody TicketDTO input) {
        Ticket ticket = ticketMapper.toEntity(input);
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

        Ticket updatedTicket = ticketMapper.toEntity(input);
        updatedTicket.setId(id);
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

    @GetMapping("/purchased")
    public List<TicketDTO> getPurchasedTickets() {
        List<Ticket> purchasedTickets = ticketService.getPurchasedTickets();

        // Ensure that when tickets are returned, their quantity is correctly included
        return purchasedTickets.stream()
                .map(ticketMapper::toDTO)  // Make sure `toDTO` includes `quantity`
                .collect(Collectors.toList());
    }
}
