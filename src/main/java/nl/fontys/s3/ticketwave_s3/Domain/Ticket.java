package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    private Integer id;
    private Integer eventId;
    private String ticketName;
    private Double price;
    private Integer quantity;
}


