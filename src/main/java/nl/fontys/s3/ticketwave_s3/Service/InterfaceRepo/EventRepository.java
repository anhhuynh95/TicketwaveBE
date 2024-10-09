package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.Event;

import java.util.List;

public interface EventRepository {
    List<Event> findAll();
    Event findById(Integer id);
    void save(Event event);
    void deleteById(Integer id);
}
