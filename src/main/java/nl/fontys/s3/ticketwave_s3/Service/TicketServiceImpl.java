package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;  // To validate associated events

    private final List<Ticket> purchasedTickets = new ArrayList<>();  // Stores purchased tickets
    private final Map<Integer, Integer> purchasedTicketQuantities = new HashMap<>();  // Tracks quantities of purchased tickets

    /** Fetch all tickets. */
    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /** Fetch a specific ticket by its ID. */
    @Override
    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id);
    }

    /** Fetch tickets filtered by a maximum price. */
    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getPrice() <= maxPrice) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    /** Create a new ticket and validate its data. */
    @Override
    public void createTicket(Ticket ticket) {
        if (ticket.getPrice() < 0) {
            throw new IllegalArgumentException("Ticket price cannot be negative.");
        }

        if (eventRepository.findById(ticket.getEventId()) == null) {
            throw new IllegalArgumentException("Event with ID " + ticket.getEventId() + " does not exist.");
        }

        ticketRepository.save(ticket);
    }

    /** Update an existing ticket by its ID. */
    @Override
    public void updateTicket(Integer id, Ticket ticket) {
        Ticket existingTicket = ticketRepository.findById(id);
        if (existingTicket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        ticket.setId(id);  // Ensure the ticket ID is set before saving
        ticketRepository.save(ticket);
    }

    /** Delete a ticket by its ID. */
    @Override
    public void deleteTicket(Integer id) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        ticketRepository.deleteById(id);
    }

    /** Process ticket purchases and update event ticket availability. */
    @Override
    public void purchaseTicket(Integer id, Integer quantity) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        if (event.getTicketQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available.");
        }

        event.setTicketQuantity(event.getTicketQuantity() - quantity);  // Deduct purchased quantity
        eventRepository.save(event);

        // Update or create purchased ticket record
        purchasedTicketQuantities.merge(id, quantity, Integer::sum);
        if (!purchasedTickets.contains(ticket)) {
            purchasedTickets.add(ticket);
        }

        // Update the ticket's quantity and save it
        ticket.setQuantity(purchasedTicketQuantities.get(id));
        ticketRepository.save(ticket);
    }

    /** Fetch the list of purchased tickets. */
    @Override
    public List<Ticket> getPurchasedTickets() {
        return new ArrayList<>(purchasedTickets);  // Return a copy of the purchased tickets list
    }
}
