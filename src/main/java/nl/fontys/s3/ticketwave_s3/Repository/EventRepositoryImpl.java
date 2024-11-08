package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final EventDBRepository eventDBRepository;

    private final EventMapper eventMapper;

    public EventRepositoryImpl(EventDBRepository eventDBRepository, EventMapper eventMapper) {
        this.eventDBRepository = eventDBRepository;
        this.eventMapper = eventMapper;
    }

    /**Retrieve all events from the in-memory store*/
    @Override
    public List<Event> findAll() {
        return eventDBRepository.findAll().stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    /**Find an event by its ID.*/
    @Override
    public Event findById(Integer id) {
        return eventDBRepository.findById(id)
                .map(eventMapper::toDomain)
                .orElse(null);
    }

    /**Save or update an event in the repository.*/
    @Override
    public void save(Event event) {
        EventEntity entity = eventMapper.toEntity(event);
        eventDBRepository.save(entity);
    }

    /**Delete an event by its ID.*/
    @Override
    public void deleteById(Integer id) {
        eventDBRepository.deleteById(id);
    }
}
