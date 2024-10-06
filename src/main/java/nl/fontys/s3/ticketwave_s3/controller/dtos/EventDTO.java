package nl.fontys.s3.ticketwave_s3.controller.dtos;

import lombok.Data;

@Data
public class EventDTO {
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String dateTime;
}
