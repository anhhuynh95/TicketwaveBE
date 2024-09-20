package nl.fontys.s3.ticketwave_s3.repository;

import nl.fontys.s3.ticketwave_s3.models.Ticket;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepo {

    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 1;

    public TicketRepo() {
        tickets.add(new Ticket(nextId++, "Concert A", "Eindhoven", 50.0));
        tickets.add(new Ticket(nextId++, "Art Exhibition", "Nuenen", 30.0));
        tickets.add(new Ticket(nextId++, "Sports Event", "Amsterdam", 75.0));
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }

    public Ticket findById(Integer id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    public List<Ticket> findByPriceLessThanMax(double maxPrice) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getPrice() <= maxPrice) {
                result.add(ticket);
            }
        }
        return result;
    }

    public void save(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(nextId++);
            tickets.add(ticket);
        } else {
            // Update existing ticket
            Ticket existingTicket = findById(ticket.getId());
            if (existingTicket != null) {
                existingTicket.setEventName(ticket.getEventName());
                existingTicket.setLocation(ticket.getLocation());
                existingTicket.setPrice(ticket.getPrice());
            }
        }
    }

    public void deleteById(Integer id) {
        Ticket ticketToDelete = findById(id);
        if (ticketToDelete != null) {
            tickets.remove(ticketToDelete);
        }
    }
}

