package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository {
    Page<Event> findAll(Pageable pageable);
    Event findById(Integer id);
    Event save(Event event);
    void deleteById(Integer id);
    Page<EventEntity> searchEvents(String query, EventType eventType, Double latitude, Double longitude, Double radius, Pageable pageable);


}
