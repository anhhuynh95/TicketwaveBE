package nl.fontys.s3.ticketwave_s3.business;

import nl.fontys.s3.ticketwave_s3.models.Ticket;
import nl.fontys.s3.ticketwave_s3.repository.TicketRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepo ticketRepo;

    public List<Ticket> getAllTickets() {
        return ticketRepo.findAll();
    }

    public Ticket getTicketById(Integer id) {
        return ticketRepo.findById(id);
    }

    public List<Ticket> getTicketsByPrice(double maxPrice) {
        return ticketRepo.findByPriceLessThanMax(maxPrice);
    }

    public void createTicket(Ticket ticket) {
        ticketRepo.save(ticket);
    }

    public void updateTicket(Integer id, Ticket updatedTicket) {
        Ticket existingTicket = getTicketById(id);
        if (existingTicket != null) {
            updatedTicket.setId(id);
            ticketRepo.save(updatedTicket);
        }
    }

    public void deleteTicket(Integer id) {
        Ticket ticket = getTicketById(id);
        if (ticket != null) {
            ticketRepo.deleteById(id);
        }
    }
}
