package nl.fontys.s3.ticketwave_s3.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Ticket;

import java.util.List;

public interface TicketRepository {
    List<Ticket> findAll();
    Ticket findById(Integer id);
    void save(Ticket ticket);
    void deleteById(Integer id);
}
