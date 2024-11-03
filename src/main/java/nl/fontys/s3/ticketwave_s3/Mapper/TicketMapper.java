package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDTO toDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setTicketName(ticket.getTicketName());
        ticketDTO.setPrice(ticket.getPrice());
        ticketDTO.setQuantity(ticket.getQuantity());
        return ticketDTO;
    }

    public Ticket toDomain(TicketDTO ticketDTO) {
        if (ticketDTO == null) {
            return null;
        }
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setTicketName(ticketDTO.getTicketName());
        ticket.setPrice(ticketDTO.getPrice());
        ticket.setQuantity(ticketDTO.getQuantity());
        return ticket;
    }

    public Ticket toDomain(TicketEntity entity) {
        if (entity == null) return null;
        Ticket ticket = new Ticket();
        ticket.setId(entity.getId());
        ticket.setEventId(entity.getEvent().getId());
        ticket.setTicketName(entity.getTicketName());
        ticket.setPrice(entity.getPrice());
        ticket.setQuantity(entity.getQuantity());
        return ticket;
    }

    public TicketEntity toEntity(Ticket ticket, EventEntity eventEntity) {
        if (ticket == null) return null;
        TicketEntity entity = new TicketEntity();
        entity.setId(ticket.getId());
        entity.setEvent(eventEntity);
        entity.setTicketName(ticket.getTicketName());
        entity.setPrice(ticket.getPrice());
        entity.setQuantity(ticket.getQuantity());
        return entity;
    }
}
