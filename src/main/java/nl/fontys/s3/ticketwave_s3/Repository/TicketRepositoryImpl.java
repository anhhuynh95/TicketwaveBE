package nl.fontys.s3.ticketwave_s3.Repository;

import jakarta.persistence.EntityManager;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketDBRepository;
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

    @Override
    public List<Ticket> findAll() {
        return ticketDBRepository.findAll().stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Ticket> findById(Integer id) {
        return ticketDBRepository.findById(id).map(ticketMapper::toDomain);
    }

    @Override
    public Optional<TicketEntity> findEntityById(Integer id) {
        return ticketDBRepository.findById(id);
    }

    @Override
    public void save(Ticket ticket, EventEntity eventEntity) {
        TicketEntity entity = ticketMapper.toEntity(ticket, eventEntity);
        ticketDBRepository.save(entity);
    }

    @Override
    public void saveEntity(TicketEntity ticketEntity) {
        entityManager.persist(ticketEntity);
    }

    @Override
    public void deleteById(Integer id) {
        ticketDBRepository.deleteById(id);
    }
    @Override
    public List<Ticket> findByEventId(Integer eventId) {
        return ticketDBRepository.findByEventId(eventId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }
}
