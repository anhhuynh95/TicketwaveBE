package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.Data;

import java.util.List;

@Data
public class EventDTO {
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String dateTime;
    private Integer ticketQuantity;
    private List<TicketDTO> tickets;
}
