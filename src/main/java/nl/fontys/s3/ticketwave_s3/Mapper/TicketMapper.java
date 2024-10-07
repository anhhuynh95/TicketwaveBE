package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.dtos.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    public TicketDTO toDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setEventName(ticket.getEventName());
        ticketDTO.setLocation(ticket.getLocation());
        ticketDTO.setPrice(ticket.getPrice());
        return ticketDTO;
    }

    public Ticket toEntity(TicketDTO ticketDTO) {
        if (ticketDTO == null) {
            return null;
        }
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setEventName(ticketDTO.getEventName());
        ticket.setLocation(ticketDTO.getLocation());
        ticket.setPrice(ticketDTO.getPrice());
        return ticket;
    }
}
