package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchasedTicketDTO {
    private Integer ticketId;
    private String ticketName;
    private Double price;
    private Integer quantity;
    private String eventName;
    private String location;
}
