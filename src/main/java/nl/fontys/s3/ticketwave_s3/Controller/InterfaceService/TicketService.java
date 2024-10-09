package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Domain.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(Integer id);
    List<Ticket> getTicketsByPrice(Double maxPrice);
    void createTicket(Ticket ticket);
    void updateTicket(Integer id, Ticket ticket);
    void deleteTicket(Integer id);
}
