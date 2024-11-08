package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(Integer id);
    List<Ticket> getTicketsByPrice(Double maxPrice);
    List<PurchasedTicketDTO> getPurchasedTickets();
    void createTicket(Ticket ticket);
    void updateTicket(Integer id, Ticket ticket);
    void deleteTicket(Integer id);
    void purchaseTicket(Integer id, Integer quantity);
    List<Ticket> getTicketsByEventId(Integer eventId);
    void cancelTickets(Integer ticketId, Integer cancelQuantity);
}
