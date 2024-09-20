package nl.fontys.s3.ticketwave_s3.business;

import nl.fontys.s3.ticketwave_s3.models.Ticket;
import nl.fontys.s3.ticketwave_s3.repository.TicketRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepo ticketRepo;

    public List<Ticket> getAllTickets() {
        return ticketRepo.findAll();
    }

    public Ticket getTicketById(Integer id) {
        Optional<Ticket> ticket = ticketRepo.findById(id);
        return ticket.orElse(null);
    }

    public List<Ticket> getTicketsByPrice(double maxPrice) {
        return ticketRepo.findByPriceLessThanMax(maxPrice);
    }

    public void createTicket(Ticket ticket) {
        ticketRepo.save(ticket);
    }
}

