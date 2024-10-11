package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    // In-memory list of tickets acting as the data store
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 1; // Counter for generating unique ticket IDs

    // Constructor to initialize some sample tickets
    public TicketRepositoryImpl() {
        tickets.add(new Ticket(nextId++, "Concert A", "Eindhoven", 50.0, 1, 1));
        tickets.add(new Ticket(nextId++, "Art Exhibition", "Nuenen", 30.0, 2, 1));
        tickets.add(new Ticket(nextId++, "Sports Event", "Amsterdam", 75.0, 3, 2));
    }

    /**Retrieve all tickets from the in-memory store.*/
    @Override
    public List<Ticket> findAll() {
        // Return a new list to protect the original list from modification
        return new ArrayList<>(tickets);
    }

    /** Find a ticket by its ID.*/
    @Override
    public Ticket findById(Integer id) {
        // Iterate through the tickets and return the ticket with matching ID
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null; // Return null if no ticket is found
    }

    /**Save or update a ticket in the repository.*/
    @Override
    public void save(Ticket ticket) {
        if (ticket.getId() == null) {
            // If the ticket is new (no ID), assign a new ID and add it to the list
            ticket.setId(nextId++);
            tickets.add(ticket);
        } else {
            // If the ticket already exists, update the existing ticket's details
            Ticket existingTicket = findById(ticket.getId());
            if (existingTicket != null) {
                existingTicket.setEventName(ticket.getEventName());
                existingTicket.setLocation(ticket.getLocation());
                existingTicket.setPrice(ticket.getPrice());
                existingTicket.setQuantity(ticket.getQuantity());
            }
        }
    }

    /**Delete a ticket by its ID.*/
    @Override
    public void deleteById(Integer id) {
        // Find and remove the ticket from the list
        Ticket ticket = findById(id);
        if (ticket != null) {
            tickets.remove(ticket);
        }
    }
}
