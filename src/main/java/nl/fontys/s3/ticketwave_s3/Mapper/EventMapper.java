package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventMapper {

    private final TicketMapper ticketMapper;

    public EventMapper(TicketMapper ticketMapper) {
        this.ticketMapper = ticketMapper;
    }

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
        dto.setTickets(tickets.stream().map(ticketMapper::toDTO).toList());
        String cloudinaryBaseUrl = "https://res.cloudinary.com/du63rfliz/image/upload/events/";
        dto.setEventType(event.getEventType());
        dto.setImageUrl(cloudinaryBaseUrl + event.getId());
        return dto;
    }
    // Convert domain Event to DTO without tickets

    public EventDTO toDTO(Event event) {
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
        dto.setEventType(event.getEventType());

        String cloudinaryBaseUrl = "https://res.cloudinary.com/du63rfliz/image/upload/events/";
        dto.setImageUrl(cloudinaryBaseUrl + event.getId());

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
        event.setEventType(dto.getEventType());
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
        entity.setEventType(event.getEventType());
        entity.setLatitude(event.getLatitude());
        entity.setLongitude(event.getLongitude());
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
        event.setEventType(entity.getEventType());
        event.setLatitude(entity.getLatitude());
        event.setLongitude(entity.getLongitude());
        return event;
    }
}
