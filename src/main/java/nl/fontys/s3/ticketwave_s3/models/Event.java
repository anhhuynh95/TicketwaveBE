package nl.fontys.s3.ticketwave_s3.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String dateTime;
}