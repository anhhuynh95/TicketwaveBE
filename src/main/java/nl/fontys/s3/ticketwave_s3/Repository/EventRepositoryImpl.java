package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.EventDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final EventDBRepository eventDBRepository;

    private final EventMapper eventMapper;

    public EventRepositoryImpl(EventDBRepository eventDBRepository, EventMapper eventMapper) {
        this.eventDBRepository = eventDBRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventDBRepository.findAll(pageable)
                .map(eventMapper::toDomain); // Map entities to domain objects
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
    public Event save(Event event) {
        EventEntity entity = eventMapper.toEntity(event);
        EventEntity savedEntity = eventDBRepository.save(entity);
        return eventMapper.toDomain(savedEntity);
    }

    /**Delete an event by its ID.*/
    @Override
    public void deleteById(Integer id) {
        eventDBRepository.deleteById(id);
    }

    @Override
    public Page<EventEntity> searchEvents(String query, EventType eventType, Double latitude, Double longitude, Double radius, Pageable pageable) {
        return eventDBRepository.searchEvents(query, eventType, latitude, longitude, radius, pageable);
    }
}
