package nl.fontys.s3.ticketwave_s3.interfaceRepo;

import nl.fontys.s3.ticketwave_s3.models.Event;

import java.util.List;

public interface EventRepository {
    List<Event> findAll();
    Event findById(Integer id);
    void save(Event event);
    void deleteById(Integer id);
}
