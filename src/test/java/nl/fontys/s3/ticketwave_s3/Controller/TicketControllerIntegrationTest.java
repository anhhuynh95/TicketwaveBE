package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.transaction.Transactional;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.EventDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.TicketDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventDBRepository eventDBRepository;

    @Autowired
    private TicketDBRepository ticketDBRepository;

    @BeforeEach
    void setUp() {
        // Check if the 'spring.profiles.active' system property is set
        String activeProfile = System.getProperty("spring.profiles.active", "test");

        if (!"test".equals(activeProfile)) {
            throw new IllegalStateException("Test is running with an incorrect profile: " + activeProfile);
        }

        // Clear database tables
        ticketDBRepository.deleteAll();
        eventDBRepository.deleteAll();

        // Create sample events
        EventEntity event1 = EventEntity.builder()
                .name("Concert")
                .location("Eindhoven")
                .description("Music concert")
                .dateTime("2025-01-01T20:00:00")
                .ticketQuantity(200)
                .eventType(EventType.MUSIC)
                .build();

        EventEntity event2 = EventEntity.builder()
                .name("Art Fair")
                .location("Rotterdam")
                .description("Art exhibition")
                .dateTime("2025-02-15T10:00:00")
                .ticketQuantity(150)
                .eventType(EventType.ART)
                .build();

        eventDBRepository.saveAll(List.of(event1, event2));

        // Create sample tickets
        TicketEntity ticket1 = TicketEntity.builder()
                .event(event1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();

        TicketEntity ticket2 = TicketEntity.builder()
                .event(event2)
                .ticketName("Standard")
                .price(50.0)
                .quantity(75)
                .build();

        ticketDBRepository.saveAll(List.of(ticket1, ticket2));
    }

    @Test
    @WithMockUser(username = "user")
    void getAllTickets_ShouldReturnAllTickets() throws Exception {
        mockMvc.perform(get("/tickets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticketName").value("VIP"))
                .andExpect(jsonPath("$[1].ticketName").value("Standard"));
    }

    @Test
    @WithMockUser(username = "user")
    void getTicket_ShouldReturnTicket() throws Exception {
        TicketEntity existingTicket = ticketDBRepository.findAll().get(0);
        Integer ticketId = existingTicket.getId();

        mockMvc.perform(get("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketName").value(existingTicket.getTicketName()))
                .andExpect(jsonPath("$.price").value(existingTicket.getPrice()))
                .andExpect(jsonPath("$.quantity").value(existingTicket.getQuantity()));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createTicket_ShouldSaveTicket() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();
        String newTicketJson = """
                {
                    "ticketName": "Premium",
                    "price": 120.0,
                    "quantity": 30
                }
                """;

        mockMvc.perform(post("/tickets/create")
                        .param("eventId", eventId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTicketJson))
                .andExpect(status().isCreated());

        assert (ticketDBRepository.findAll().size() == 3);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteTicket_ShouldRemoveTicket() throws Exception {
        Integer ticketId = ticketDBRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/tickets/{id}", ticketId))
                .andExpect(status().isNoContent());

        assert (ticketDBRepository.findById(ticketId).isEmpty());
    }

    @Test
    @WithMockUser(username = "user")
    void getTicketsByEventId_ShouldReturnTicketsForEvent() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        mockMvc.perform(get("/tickets/by-event/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticketName").value("VIP"));
    }


}

