package nl.fontys.s3.ticketwave_s3.Controller;

import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenDecoder;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.EventDTO;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.TicketDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CloudinaryService cloudinaryService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @Test
    @WithMockUser(username = "user")
    void getAllEvents_shouldReturn200ResponseWithPaginatedEventsArray() throws Exception {
        Event event1 = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .build();
        Event event2 = Event.builder()
                .id(2)
                .name("Festival")
                .location("Rotterdam")
                .build();
        List<Event> events = List.of(event1, event2);

        Ticket ticket1 = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        Ticket ticket2 = Ticket.builder()
                .id(2)
                .eventId(2)
                .ticketName("Standard")
                .price(50.0)
                .quantity(100)
                .build();
        List<Ticket> ticketsEvent1 = List.of(ticket1);
        List<Ticket> ticketsEvent2 = List.of(ticket2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(events, pageable, events.size());

        when(eventService.getAllEvents(any(Pageable.class))).thenReturn(eventPage);
        when(ticketService.getTicketsByEventId(1)).thenReturn(ticketsEvent1);
        when(ticketService.getTicketsByEventId(2)).thenReturn(ticketsEvent2);

        when(cloudinaryService.generateImageUrl("1")).thenReturn("https://res.cloudinary.com/du63rfliz/image/upload/events/1");
        when(cloudinaryService.generateImageUrl("2")).thenReturn("https://res.cloudinary.com/du63rfliz/image/upload/events/2");

        when(eventMapper.toDTO(event1, ticketsEvent1)).thenReturn(
                EventDTO.builder()
                        .id(1)
                        .name("Concert")
                        .location("Amsterdam")
                        .imageUrl("https://res.cloudinary.com/du63rfliz/image/upload/events/1")
                        .tickets(List.of(TicketDTO.builder().id(1).ticketName("VIP").price(100.0).quantity(50).build()))
                        .build()
        );
        when(eventMapper.toDTO(event2, ticketsEvent2)).thenReturn(
                EventDTO.builder()
                        .id(2)
                        .name("Festival")
                        .location("Rotterdam")
                        .imageUrl("https://res.cloudinary.com/du63rfliz/image/upload/events/2")
                        .tickets(List.of(TicketDTO.builder().id(2).ticketName("Standard").price(50.0).quantity(100).build()))
                        .build()
        );

        mockMvc.perform(get("/events")
                        .param("page", "0")
                        .param("size", "10")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
            {
                "content": [
                    {
                        "id": 1,
                        "name": "Concert",
                        "location": "Amsterdam",
                        "imageUrl": "https://res.cloudinary.com/du63rfliz/image/upload/events/1",
                        "tickets": [
                            {
                                "id": 1,
                                "ticketName": "VIP",
                                "price": 100.0,
                                "quantity": 50
                            }
                        ]
                    },
                    {
                        "id": 2,
                        "name": "Festival",
                        "location": "Rotterdam",
                        "imageUrl": "https://res.cloudinary.com/du63rfliz/image/upload/events/2",
                        "tickets": [
                            {
                                "id": 2,
                                "ticketName": "Standard",
                                "price": 50.0,
                                "quantity": 100
                            }
                        ]
                    }
                ],
                "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "totalElements": 2,
                "totalPages": 1
            }
            """));

        verify(eventService).getAllEvents(any(Pageable.class));
        verify(ticketService).getTicketsByEventId(1);
        verify(ticketService).getTicketsByEventId(2);
        verify(cloudinaryService).generateImageUrl("1");
        verify(cloudinaryService).generateImageUrl("2");
    }

    @Test
    @WithMockUser(username = "user")
    void getEvent_shouldReturn200ResponseWithEvent() throws Exception {
        int eventId = 1;
        Event event = Event.builder()
                .id(eventId)
                .name("Concert")
                .location("Amsterdam")
                .build();
        Ticket ticket = Ticket.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        TicketDTO ticketDTO = TicketDTO.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(50)
                .build();
        EventDTO eventDTO = EventDTO.builder()
                .id(eventId)
                .name("Concert")
                .location("Amsterdam")
                .imageUrl("https://res.cloudinary.com/du63rfliz/image/upload/events/1")
                .tickets(List.of(ticketDTO))
                .build();

        when(eventService.getEventById(eventId)).thenReturn(event);
        when(ticketService.getTicketsByEventId(eventId)).thenReturn(List.of(ticket));
        when(eventMapper.toDTO(event, List.of(ticket))).thenReturn(eventDTO);
        when(cloudinaryService.generateImageUrl(String.valueOf(eventId)))
                .thenReturn("https://res.cloudinary.com/du63rfliz/image/upload/events/1");

        mockMvc.perform(get("/events/{id}", eventId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                {
                    "id": 1,
                    "name": "Concert",
                    "location": "Amsterdam",
                    "imageUrl": "https://res.cloudinary.com/du63rfliz/image/upload/events/1",
                    "tickets": [
                        {
                            "id": 1,
                            "ticketName": "VIP",
                            "price": 100.0,
                            "quantity": 50
                        }
                    ]
                }
            """));

        verify(eventService).getEventById(eventId);
        verify(ticketService).getTicketsByEventId(eventId);
        verify(cloudinaryService).generateImageUrl(String.valueOf(eventId));
        verifyNoMoreInteractions(ticketService, cloudinaryService);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createEvent_shouldCreateAndReturn201_WhenRequestValid() throws Exception {
        EventDTO eventDTO = EventDTO.builder()
                .name("Concert")
                .location("Amsterdam")
                .dateTime("2024-12-31T19:00:00")
                .build();

        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .dateTime("2024-12-31T19:00:00")
                .build();

        when(eventMapper.toDomain(eventDTO)).thenReturn(event);

        mockMvc.perform(post("/events")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {
                            "name": "Concert",
                            "location": "Amsterdam",
                            "dateTime": "2024-12-31T19:00:00"
                        }
                    """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(eventMapper).toDomain(eventDTO);
        verify(eventService).createEvent(event);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createEvent_shouldNotCreateAndReturn400_WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {
                            "name": "",
                            "location": "",
                            "dateTime": ""
                        }
                    """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                {
                    "name": "must not be blank",
                    "location": "must not be blank",
                    "dateTime": "must not be blank"
                }
            """));

        verifyNoInteractions(eventService, eventMapper);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void updateEvent_shouldUpdateEventAndReturn204() throws Exception {
        int eventId = 1;
        EventDTO eventDTO = EventDTO.builder()
                .name("Updated Concert")
                .location("Updated Location")
                .dateTime("2024-12-31T20:00:00")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Updated Concert")
                .location("Updated Location")
                .dateTime("2024-12-31T20:00:00")
                .build();

        when(eventMapper.toDomain(eventDTO)).thenReturn(event);

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {
                            "name": "Updated Concert",
                            "location": "Updated Location",
                            "dateTime": "2024-12-31T20:00:00"
                        }
                    """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(eventMapper).toDomain(eventDTO);
        verify(eventService).updateEvent(eventId, event);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteEvent_shouldDeleteEventAndReturn204() throws Exception {
        int eventId = 1;

        mockMvc.perform(delete("/events/{id}", eventId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(eventId);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteEvent_shouldReturn404WhenEventNotFound() throws Exception {
        int eventId = 999;
        doThrow(new RuntimeException("Event not found")).when(eventService).deleteEvent(eventId);

        mockMvc.perform(delete("/events/{id}", eventId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));

        verify(eventService).deleteEvent(eventId);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void uploadEventImage_shouldReturn200AndImageUrl_whenValidRequest() throws Exception {
        int eventId = 1;
        String expectedImageUrl = "https://res.cloudinary.com/du63rfliz/image/upload/events/1";

        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "Sample image content".getBytes()
        );

        Path mockTempDir = Files.createTempDirectory("mock-secure-temp-dir");
        Path mockTempFile = mockTempDir.resolve("mock-temp-file.jpg");

        // Mock the directory creation and file behavior
        when(cloudinaryService.createSecureTempDirectory()).thenReturn(mockTempDir);
        when(cloudinaryService.uploadEventImage(any(File.class), eq(String.valueOf(eventId))))
                .thenReturn(expectedImageUrl);

        mockMvc.perform(multipart("/events/{eventId}/uploadImage", eventId)
                        .file(mockImage)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(expectedImageUrl));

        verify(cloudinaryService).uploadEventImage(any(File.class), eq(String.valueOf(eventId)));

        // Cleanup
        Files.deleteIfExists(mockTempFile);
        Files.deleteIfExists(mockTempDir);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void uploadEventImage_shouldReturn400_whenNoImageProvided() throws Exception {
        int eventId = 1;

        mockMvc.perform(multipart("/events/{eventId}/uploadImage", eventId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void uploadEventImage_shouldReturn400_whenInvalidFileTypeProvided() throws Exception {
        int eventId = 1;
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "Invalid file content".getBytes()
        );

        mockMvc.perform(multipart("/events/{eventId}/uploadImage", eventId)
                        .file(invalidFile)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cloudinaryService);
    }

}
