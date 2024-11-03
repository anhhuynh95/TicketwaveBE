package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.Data;

@Data
public class TicketDTO {
    private Integer id;
    private Integer eventId;
    private String ticketName;
    private Double price;
    private Integer quantity;
}

