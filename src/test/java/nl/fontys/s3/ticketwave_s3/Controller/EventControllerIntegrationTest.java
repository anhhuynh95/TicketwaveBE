package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.EventDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.PurchasedTicketRepository;
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
@ActiveProfiles("test")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventDBRepository eventDBRepository;

    @Autowired
    private TicketDBRepository ticketDBRepository;

    @Autowired
    private PurchasedTicketRepository purchasedTicketRepository;

    @Autowired
    private CommentDBRepository commentDBRepository;

    @BeforeEach
    void setUp() {
        // Check if the 'spring.profiles.active' system property is set
        String activeProfile = System.getProperty("spring.profiles.active", "test");

        if (!"test".equals(activeProfile)) {
            throw new IllegalStateException("Test is running with an incorrect profile: " + activeProfile);
        }

        // Clear database tables
        commentDBRepository.deleteAll();
        purchasedTicketRepository.deleteAll();
        ticketDBRepository.deleteAll();
        eventDBRepository.deleteAll();

        EventEntity event1 = EventEntity.builder()
                .name("Music Festival")
                .location("Amsterdam")
                .description("A grand music festival")
                .dateTime("2025-01-15T18:00:00")
                .ticketQuantity(100)
                .eventType(EventType.MUSIC)
                .build();

        EventEntity event2 = EventEntity.builder()
                .name("Art Expo")
                .location("Rotterdam")
                .description("A stunning art exhibition")
                .dateTime("2025-02-20T10:00:00")
                .ticketQuantity(50)
                .eventType(EventType.ART)
                .build();

        eventDBRepository.saveAll(List.of(event1, event2));
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() throws Exception {
        mockMvc.perform(get("/events")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Music Festival"))
                .andExpect(jsonPath("$.content[1].name").value("Art Expo"));
    }

    @Test
    @WithMockUser(username = "user")
    void getEvent_ShouldReturnSpecificEvent() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        mockMvc.perform(get("/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Music Festival"))
                .andExpect(jsonPath("$.location").value("Amsterdam"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createEvent_ShouldSaveEvent() throws Exception {
        String newEventJson = """
                {
                    "name": "Food Carnival",
                    "location": "Utrecht",
                    "description": "A carnival for food lovers",
                    "dateTime": "2025-03-10T12:00:00",
                    "ticketQuantity": 200,
                    "eventType": "FOOD"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newEventJson))
                .andExpect(status().isCreated());

        assert (eventDBRepository.findAll().size() == 3);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void updateEvent_ShouldUpdateEvent() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        String updatedEventJson = """
                {
                    "name": "Updated Music Festival",
                    "location": "Utrecht",
                    "description": "An updated music festival description",
                    "dateTime": "2025-01-20T18:00:00",
                    "ticketQuantity": 150,
                    "eventType": "MUSIC"
                }
                """;

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEventJson))
                .andExpect(status().isNoContent());

        EventEntity updatedEvent = eventDBRepository.findById(eventId).orElseThrow();
        assert (updatedEvent.getName().equals("Updated Music Festival"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteEvent_ShouldRemoveEvent() throws Exception {
        Integer eventId = eventDBRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isNoContent());

        assert (eventDBRepository.findById(eventId).isEmpty());
    }

    @Test
    void searchEvents_ShouldReturnMatchingEvents() throws Exception {
        mockMvc.perform(get("/events/search")
                        .param("query", "Music")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Music Festival"));
    }
}
