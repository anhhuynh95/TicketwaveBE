package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TicketRepositoryImpl implements TicketRepository {

    @Autowired
    private TicketDBRepository ticketDBRepository;

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public List<Ticket> findAll() {
        return ticketDBRepository.findAll().stream()
                .map(ticketMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Ticket findById(Integer id) {
        TicketEntity entity = ticketDBRepository.findById(id).orElse(null);
        return entity != null ? ticketMapper.toDomain(entity) : null;
    }

    @Override
    public void save(Ticket ticket, EventEntity eventEntity) {
        TicketEntity entity = ticketMapper.toEntity(ticket, eventEntity);
        ticketDBRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        ticketDBRepository.deleteById(id);
    }
    @Override
    public List<Ticket> findByEventId(Integer eventId) {
        return ticketDBRepository.findByEventId(eventId).stream()
                .map(ticketMapper::toDomain)
                .collect(Collectors.toList());
    }
}
