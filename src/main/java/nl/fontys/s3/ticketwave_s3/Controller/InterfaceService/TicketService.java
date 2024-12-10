package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(Integer id);
    List<Ticket> getTicketsByPrice(Double maxPrice);
    List<PurchasedTicketDTO> getPurchasedTickets(Integer userId);
    void createTicket(Ticket ticket);
    void updateTicket(Integer id, Ticket ticket);
    void deleteTicket(Integer id);
    void purchaseTicket(Integer id, Integer quantity, Integer userId);
    List<Ticket> getTicketsByEventId(Integer eventId);
    void cancelTickets(Integer ticketId, Integer cancelQuantity);
    Map<String, Long> getTotalPurchasesByEventType();
    Map<String, Double> getMonthlySales(LocalDateTime startDate);

}
