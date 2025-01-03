package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    List<Ticket> findAll();
    Optional<Ticket> findById(Integer id);
    Optional<TicketEntity> findEntityById(Integer id);
    void save(Ticket ticket, EventEntity eventEntity);
    void saveEntity(TicketEntity ticketEntity);
    void deleteById(Integer id);
    List<Ticket> findByEventId(Integer eventId);

}
