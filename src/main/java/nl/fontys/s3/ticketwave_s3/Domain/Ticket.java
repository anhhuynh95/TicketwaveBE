package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Ticket {
    private Integer id;
    private String eventName;
    private String location;
    private Double price;
    private Integer eventId;
    private Integer quantity;
}
