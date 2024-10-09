package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TicketsDTO {
    private List<TicketDTO> tickets = new ArrayList<>();
}
