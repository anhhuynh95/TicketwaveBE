package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String dateTime;
    private Integer ticketQuantity;
    private EventType eventType;
}