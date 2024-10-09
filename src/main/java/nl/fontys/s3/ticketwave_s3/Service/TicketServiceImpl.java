package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository; // Add this to validate events

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id);
    }
    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getPrice() <= maxPrice) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    @Override
    public void createTicket(Ticket ticket) {
        // Validate price
        if (ticket.getPrice() < 0) {
            throw new IllegalArgumentException("Ticket price cannot be negative.");
        }
        // Validate the associated event exists
        if (eventRepository.findById(ticket.getEventId()) == null) {
            throw new IllegalArgumentException("Event with ID " + ticket.getEventId() + " does not exist.");
        }
        ticketRepository.save(ticket);
    }

    @Override
    public void updateTicket(Integer id, Ticket ticket) {
        Ticket existingTicket = ticketRepository.findById(id);
        if (existingTicket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        ticket.setId(id); // Ensure the ID is set to the existing ticket
        ticketRepository.save(ticket);
    }

    @Override
    public void deleteTicket(Integer id) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        ticketRepository.deleteById(id);
    }
}
