package nl.fontys.s3.ticketwave_s3.Repository;

import jakarta.persistence.EntityManager;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.TicketDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketRepositoryImpl implements TicketRepository {

    private final EntityManager entityManager;

    private final TicketDBRepository ticketDBRepository;

    private final TicketMapper ticketMapper;

    public TicketRepositoryImpl(EntityManager entityManager, TicketDBRepository ticketDBRepository, TicketMapper ticketMapper) {
        this.entityManager = entityManager;
        this.ticketDBRepository = ticketDBRepository;
        this.ticketMapper = ticketMapper;
    }

    /** Retrieve all tickets and convert to domain objects. */
    @Override
    public List<Ticket> findAll() {
        return ticketDBRepository.findAll().stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    /** Find a ticket by ID and convert to a domain object. */
    @Override
    public Optional<Ticket> findById(Integer id) {
        return ticketDBRepository.findById(id).map(ticketMapper::toDomain);
    }

    /** Find a ticket entity by ID. */
    @Override
    public Optional<TicketEntity> findEntityById(Integer id) {
        return ticketDBRepository.findById(id);
    }

    /** Save a ticket by converting it to an entity. */
    @Override
    public void save(Ticket ticket, EventEntity eventEntity) {
        TicketEntity entity = ticketMapper.toEntity(ticket, eventEntity);
        ticketDBRepository.save(entity);
    }

    /** Persist a ticket entity directly. */
    @Override
    public void saveEntity(TicketEntity ticketEntity) {
        entityManager.persist(ticketEntity);
    }

    /** Delete a ticket by ID. */
    @Override
    public void deleteById(Integer id) {
        ticketDBRepository.deleteById(id);
    }

    /** Find tickets by event ID and convert to domain objects. */
    @Override
    public List<Ticket> findByEventId(Integer eventId) {
        return ticketDBRepository.findByEventId(eventId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }
}
