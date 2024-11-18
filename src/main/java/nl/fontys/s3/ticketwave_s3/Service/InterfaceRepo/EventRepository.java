package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository {
    Page<Event> findAll(Pageable pageable);
    Event findById(Integer id);
    void save(Event event);
    void deleteById(Integer id);
}
