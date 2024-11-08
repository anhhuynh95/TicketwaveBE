package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private Integer id;
    private Integer eventId;
    private String ticketName;
    private Double price;
    private Integer quantity;
    private Integer purchasedQuantity;
}

