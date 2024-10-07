package nl.fontys.s3.ticketwave_s3.Controller.dtos;

import lombok.Data;

@Data
public class TicketDTO {
    private Integer id;
    private String eventName;
    private String location;
    private Double price;
}
