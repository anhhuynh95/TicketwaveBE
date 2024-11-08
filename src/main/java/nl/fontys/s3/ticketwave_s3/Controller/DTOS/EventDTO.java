package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private Integer id;
    @NotBlank(message = "must not be blank")
    private String name;
    @NotBlank(message = "must not be blank")
    private String location;
    private String description;
    @NotBlank(message = "must not be blank")
    private String dateTime;
    private Integer ticketQuantity;
    private List<TicketDTO> tickets;
}
