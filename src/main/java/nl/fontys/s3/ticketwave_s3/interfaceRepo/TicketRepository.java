package nl.fontys.s3.ticketwave_s3.interfaceRepo;

import nl.fontys.s3.ticketwave_s3.models.Ticket;

import java.util.List;

public interface TicketRepository {
    List<Ticket> findAll();
    Ticket findById(Integer id);
    void save(Ticket ticket);
    void deleteById(Integer id);
}
