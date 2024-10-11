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
    private EventRepository eventRepository; // Add this to validate events

    private final List<Ticket> purchasedTickets = new ArrayList<>();
    private final Map<Integer, Integer> purchasedTicketQuantities = new HashMap<>();

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
        if (ticket.getPrice() < 0) {
            throw new IllegalArgumentException("Ticket price cannot be negative.");
        }
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
        ticket.setId(id);
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

    @Override
    public void purchaseTicket(Integer id, Integer quantity) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        // Retrieve event to check available tickets
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        // Check if enough tickets are available
        if (event.getTicketQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available.");
        }

        // Deduct the purchased quantity from the event's available tickets
        event.setTicketQuantity(event.getTicketQuantity() - quantity);
        eventRepository.save(event);

        // Check if the user already purchased this ticket (or event)
        // If yes, update the quantity, otherwise, create a new ticket entry
        if (purchasedTicketQuantities.containsKey(id)) {
            // Add the new quantity to the existing quantity
            purchasedTicketQuantities.put(id, purchasedTicketQuantities.get(id) + quantity);
        } else {
            purchasedTicketQuantities.put(id, quantity);  // Create a new entry for the purchased ticket
            purchasedTickets.add(ticket);  // Add the ticket to the purchased list
        }

        // Update the ticket's quantity in the repository
        ticket.setQuantity(purchasedTicketQuantities.get(id));  // Update ticket's quantity with the accumulated total
        ticketRepository.save(ticket);  // Save the ticket with updated quantity
    }

    @Override
    public List<Ticket> getPurchasedTickets() {
        return new ArrayList<>(purchasedTickets);
    }
}