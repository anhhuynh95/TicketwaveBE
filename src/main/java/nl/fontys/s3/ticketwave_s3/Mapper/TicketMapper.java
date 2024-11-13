package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    /** Convert Ticket domain object to TicketDTO. */
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

    /** Convert TicketDTO to Ticket domain object. */
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

    /** Convert TicketEntity to Ticket domain object. */
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

    /** Convert Ticket domain object to TicketEntity for database storage. */
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

    /** Convert PurchasedTicketEntity to PurchasedTicketDTO. */
    public PurchasedTicketDTO toPurchasedTicketDTO(PurchasedTicketEntity purchasedTicketEntity) {
        if (purchasedTicketEntity == null) return null;
        PurchasedTicketDTO purchasedTicketDTO = new PurchasedTicketDTO();
        TicketEntity ticketEntity = purchasedTicketEntity.getTicket();
        EventEntity eventEntity = ticketEntity.getEvent();

        purchasedTicketDTO.setTicketId(ticketEntity.getId());
        purchasedTicketDTO.setTicketName(ticketEntity.getTicketName());
        purchasedTicketDTO.setPrice(ticketEntity.getPrice());
        purchasedTicketDTO.setQuantity(purchasedTicketEntity.getPurchaseQuantity());
        purchasedTicketDTO.setEventName(eventEntity.getName());
        purchasedTicketDTO.setLocation(eventEntity.getLocation());

        return purchasedTicketDTO;
    }
}
