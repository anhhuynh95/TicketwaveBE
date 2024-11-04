package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    @Autowired
    private TicketMapper ticketMapper;

    // Convert domain Event to DTO
    public EventDTO toDTO(Event event, List<Ticket> tickets) {
        if (event == null) {
            return null;
        }
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setLocation(event.getLocation());
        dto.setDescription(event.getDescription());
        dto.setDateTime(event.getDateTime());
        dto.setTicketQuantity(event.getTicketQuantity());
        dto.setTickets(tickets.stream().map(ticketMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }

    // Convert DTO to domain Event
    public Event toDomain(EventDTO dto) {
        if (dto == null) {
            return null;
        }
        Event event = new Event();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setLocation(dto.getLocation());
        event.setDescription(dto.getDescription());
        event.setDateTime(dto.getDateTime());
        event.setTicketQuantity(dto.getTicketQuantity());
        return event;
    }

    // Convert domain Event to entity
    public EventEntity toEntity(Event event) {
        if (event == null) {
            return null;
        }
        EventEntity entity = new EventEntity();
        entity.setId(event.getId());
        entity.setName(event.getName());
        entity.setLocation(event.getLocation());
        entity.setDescription(event.getDescription());
        entity.setDateTime(event.getDateTime());
        entity.setTicketQuantity(event.getTicketQuantity());
        return entity;
    }

    // Convert entity to domain Event
    public Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }
        Event event = new Event();
        event.setId(entity.getId());
        event.setName(entity.getName());
        event.setLocation(entity.getLocation());
        event.setDescription(entity.getDescription());
        event.setDateTime(entity.getDateTime());
        event.setTicketQuantity(entity.getTicketQuantity());
        return event;
    }
}
