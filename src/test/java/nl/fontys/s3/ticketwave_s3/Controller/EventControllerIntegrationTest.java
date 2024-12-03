//package nl.fontys.s3.ticketwave_s3.Controller;
//
//import nl.fontys.s3.ticketwave_s3.Domain.EventType;
//import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
//import nl.fontys.s3.ticketwave_s3.Repository.JPA.EventDBRepository;
//import nl.fontys.s3.ticketwave_s3.TicketwaveS3Application;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(classes = TicketwaveS3Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//public class EventControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private EventDBRepository eventDBRepository;
//
//    @BeforeEach
//    void setUp() {
//        eventDBRepository.deleteAll();
//        eventDBRepository.save(EventEntity.builder()
//                .name("Concert")
//                .location("Amsterdam")
//                .dateTime("2024-12-25T19:00:00")
//                .ticketQuantity(100)
//                .eventType(EventType.MUSIC)
//                .build());
//    }
//
//    @Test
//    void getAllEvents_shouldReturnEvents() throws Exception {
//        mockMvc.perform(get("/events")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.content[0].name").value("Concert"))
//                .andExpect(jsonPath("$.content[0].location").value("Amsterdam"));
//    }
//
//    @Test
//    void searchEvents_shouldReturnFilteredEvents() throws Exception {
//        mockMvc.perform(get("/events/search")
//                        .param("query", "Concert")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.content[0].name").value("Concert"));
//    }
//}