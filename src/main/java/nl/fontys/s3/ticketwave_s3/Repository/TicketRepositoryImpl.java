package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 1;

    public TicketRepositoryImpl() {
        // Sample data for tickets
        tickets.add(new Ticket(nextId++, "Concert A", "Eindhoven", 50.0, 1));
        tickets.add(new Ticket(nextId++, "Art Exhibition", "Nuenen", 30.0, 2));
        tickets.add(new Ticket(nextId++, "Sports Event", "Amsterdam", 75.0, 3));
    }

    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }

    @Override
    public Ticket findById(Integer id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    @Override
    public void save(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(nextId++);
            tickets.add(ticket);
        } else {
            Ticket existingTicket = findById(ticket.getId());
            if (existingTicket != null) {
                existingTicket.setEventName(ticket.getEventName());
                existingTicket.setLocation(ticket.getLocation());
                existingTicket.setPrice(ticket.getPrice());
                existingTicket.setEventId(ticket.getEventId());
            } else {
                throw new IllegalArgumentException("Ticket with ID " + ticket.getId() + " does not exist.");
            }
        }
    }

    @Override
    public void deleteById(Integer id) {
        Ticket ticket = findById(id);
        if (ticket != null) {
            tickets.remove(ticket);
        }
    }
}

