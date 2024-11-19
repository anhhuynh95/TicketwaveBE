package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketMapper ticketMapper;

    @MockBean
    private EventService eventService;

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getAllTickets_shouldReturn200AndListOfTickets() throws Exception {
        Ticket ticket = Ticket.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .build();
        TicketDTO ticketDTO = TicketDTO.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .build();

        when(ticketService.getAllTickets()).thenReturn(List.of(ticket));
        when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

        mockMvc.perform(get("/tickets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":1,"ticketName":"VIP","price":100.0}]
                    """));

        verify(ticketService).getAllTickets();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getAllTickets_shouldReturn404WhenNoTicketsAvailable() throws Exception {
        when(ticketService.getAllTickets()).thenReturn(List.of());

        mockMvc.perform(get("/tickets"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(ticketService).getAllTickets();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getTicket_shouldReturn200WhenTicketExists() throws Exception {
        int ticketId = 1;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .ticketName("VIP")
                .price(100.0)
                .build();
        TicketDTO ticketDTO = TicketDTO.builder()
                .id(ticketId)
                .ticketName("VIP")
                .price(100.0)
                .build();

        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);
        when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

        mockMvc.perform(get("/tickets/{id}", ticketId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id":1,"ticketName":"VIP","price":100.0}
                    """));

        verify(ticketService).getTicketById(ticketId);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getTicket_shouldReturn400WhenIdIsInvalid() throws Exception {
        int invalidId = -1;

        mockMvc.perform(get("/tickets/{id}", invalidId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ID must be a positive integer."));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getTicketsByEventId_shouldReturn200AndListOfTickets() throws Exception {
        int eventId = 1;
        Ticket ticket1 = Ticket.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        Ticket ticket2 = Ticket.builder()
                .id(2)
                .ticketName("Standard")
                .price(50.0)
                .quantity(100)
                .build();
        List<Ticket> tickets = List.of(ticket1, ticket2);

        TicketDTO ticketDTO1 = TicketDTO.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        TicketDTO ticketDTO2 = TicketDTO.builder()
                .id(2)
                .ticketName("Standard")
                .price(50.0)
                .quantity(100)
                .build();

        when(ticketService.getTicketsByEventId(eventId)).thenReturn(tickets);
        when(ticketMapper.toDTO(ticket1)).thenReturn(ticketDTO1);
        when(ticketMapper.toDTO(ticket2)).thenReturn(ticketDTO2);

        mockMvc.perform(get("/tickets/by-event/{eventId}", eventId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    [
                        {
                            "id": 1,
                            "ticketName": "VIP",
                            "price": 100.0,
                            "quantity": 50
                        },
                        {
                            "id": 2,
                            "ticketName": "Standard",
                            "price": 50.0,
                            "quantity": 100
                        }
                    ]
                """));

        verify(ticketService).getTicketsByEventId(eventId);
        verify(ticketMapper).toDTO(ticket1);
        verify(ticketMapper).toDTO(ticket2);
    }


    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createTicket_shouldReturn201WhenRequestIsValid() throws Exception {
        int eventId = 1;
        TicketDTO ticketDTO = TicketDTO.builder()
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        Ticket ticket = Ticket.builder()
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        Event event = Event.builder()
                .id(eventId)
                .name("Concert")
                .ticketQuantity(100)
                .build();

        List<Ticket> existingTickets = List.of(
                Ticket.builder().quantity(20).build()
        );

        when(eventService.getEventById(eventId)).thenReturn(event);
        when(ticketService.getTicketsByEventId(eventId)).thenReturn(existingTickets);
        when(ticketMapper.toDomain(ticketDTO)).thenReturn(ticket);

        mockMvc.perform(post("/tickets/create")
                        .param("eventId", String.valueOf(eventId))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {"ticketName": "VIP", "price": 100.0, "quantity": 50}
                        """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(ticketService).createTicket(ticket);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createTicket_shouldReturn400WhenQuantityIsNull() throws Exception {
        mockMvc.perform(post("/tickets/create")
                        .param("eventId", "1")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {"ticketName": "VIP", "price": 100.0}
                        """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity must be a positive integer."));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createTicket_shouldReturn400WhenExceedsAvailableQuantity() throws Exception {
        int eventId = 1;
        Event event = Event.builder()
                .id(eventId)
                .name("Concert")
                .ticketQuantity(100)
                .build();

        List<Ticket> existingTickets = List.of(
                Ticket.builder()
                        .quantity(80)
                        .build()
        );

        when(eventService.getEventById(eventId)).thenReturn(event);
        when(ticketService.getTicketsByEventId(eventId)).thenReturn(existingTickets);

        mockMvc.perform(post("/tickets/create")
                        .param("eventId", String.valueOf(eventId))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {"ticketName": "VIP", "price": 100.0, "quantity": 30}
                        """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Exceeds available ticket quantity."));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void updateTicket_shouldReturn204WhenTicketExists() throws Exception {
        int ticketId = 1;
        TicketDTO ticketDTO = TicketDTO.builder()
                .ticketName("VIP")
                .price(120.0)
                .build();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .ticketName("VIP")
                .price(120.0)
                .build();

        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);
        when(ticketMapper.toDomain(ticketDTO)).thenReturn(ticket);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                                {"ticketName": "VIP", "price": 120.0}
                                """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(ticketService).updateTicket(ticketId, ticket);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void updateTicket_shouldReturn400WhenIdIsInvalid() throws Exception {
        int invalidId = -1;

        mockMvc.perform(put("/tickets/{id}", invalidId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                                {"ticketName": "VIP", "price": 120.0}
                                """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ID must be a positive integer."));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteTicket_shouldReturn204WhenTicketExists() throws Exception {
        int ticketId = 1;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .build();

        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);

        mockMvc.perform(delete("/tickets/{id}", ticketId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(ticketService).deleteTicket(ticketId);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteTicket_shouldReturn404WhenTicketDoesNotExist() throws Exception {
        int nonExistentId = 999;

        when(ticketService.getTicketById(nonExistentId)).thenReturn(null);

        mockMvc.perform(delete("/tickets/{id}", nonExistentId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ticket not found."));

        verify(ticketService).getTicketById(nonExistentId);
    }

    @Test
    @WithMockUser(username = "user")
    void purchaseTicket_shouldReturn200WhenRequestValid() throws Exception {
        int eventId = 1;
        int ticketId = 1;
        int quantity = 2;

        mockMvc.perform(put("/tickets/{eventId}/{ticketId}/purchase", eventId, ticketId)
                        .param("quantity", String.valueOf(quantity))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(ticketService).purchaseTicket(ticketId, quantity);
    }
    @Test
    @WithMockUser(username = "user")
    void purchaseTicket_shouldReturn400WhenQuantityIsInvalid() throws Exception {
        int eventId = 1;
        int ticketId = 1;
        int invalidQuantity = -1;

        mockMvc.perform(put("/tickets/{eventId}/{ticketId}/purchase", eventId, ticketId)
                        .param("quantity", String.valueOf(invalidQuantity))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).purchaseTicket(anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = "user")
    void purchaseTicket_shouldReturn404WhenTicketNotFound() throws Exception {
        int eventId = 1;
        int nonExistingTicketId = 999;
        int quantity = 2;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"))
                .when(ticketService).purchaseTicket(nonExistingTicketId, quantity);

        mockMvc.perform(put("/tickets/{eventId}/{ticketId}/purchase", eventId, nonExistingTicketId)
                        .param("quantity", String.valueOf(quantity))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(ticketService).purchaseTicket(nonExistingTicketId, quantity);
    }
    @Test
    @WithMockUser(username = "user")
    void cancelTicket_shouldReturn204WhenRequestIsValid() throws Exception {
        int ticketId = 1;
        int cancelQuantity = 1;

        mockMvc.perform(put("/tickets/{ticketId}/cancel", ticketId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                                {"cancelQuantity": 1}
                                """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(ticketService).cancelTickets(ticketId, cancelQuantity);
    }

    @Test
    @WithMockUser(username = "user")
    void cancelTicket_shouldReturn400WhenCancelQuantityIsInvalid() throws Exception {
        int ticketId = 1;

        mockMvc.perform(put("/tickets/{ticketId}/cancel", ticketId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                            {"cancelQuantity": 0}
                            """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).cancelTickets(anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = "user")
    void cancelTicket_shouldReturn404WhenTicketNotFound() throws Exception {
        int nonExistingTicketId = 999;
        int cancelQuantity = 1;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"))
                .when(ticketService).cancelTickets(nonExistingTicketId, cancelQuantity);

        mockMvc.perform(put("/tickets/{ticketId}/cancel", nonExistingTicketId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                            {"cancelQuantity": 1}
                            """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(ticketService).cancelTickets(nonExistingTicketId, cancelQuantity);
    }

}