package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;

import java.util.List;

public interface TicketRepository {
    List<Ticket> findAll();
    Ticket findById(Integer id);
    void save(Ticket ticket, EventEntity eventEntity);
    void deleteById(Integer id);
    List<Ticket> findByEventId(Integer eventId);
}
