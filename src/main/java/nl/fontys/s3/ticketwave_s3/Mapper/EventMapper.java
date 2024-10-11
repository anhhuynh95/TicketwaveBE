package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

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
        return dto;
    }

    public Event toEntity(EventDTO dto) {
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
}
