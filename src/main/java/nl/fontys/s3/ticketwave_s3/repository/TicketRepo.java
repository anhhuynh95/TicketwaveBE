package nl.fontys.s3.ticketwave_s3.repository;

import nl.fontys.s3.ticketwave_s3.models.Ticket;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Ticket> findById(Integer id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
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

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(nextId++);
        }
        tickets.add(ticket);
        return ticket;
    }
}

