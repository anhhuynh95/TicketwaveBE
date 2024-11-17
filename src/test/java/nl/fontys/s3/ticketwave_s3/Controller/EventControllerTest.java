package nl.fontys.s3.ticketwave_s3.Controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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

    @Test
    void getAllEvents_shouldReturn200ResponseWithEventsArray() throws Exception {
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

        when(eventService.getAllEvents()).thenReturn(events);
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

        mockMvc.perform(get("/events"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
            [
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
            ]
            """));

        verify(eventService).getAllEvents();
        verify(ticketService).getTicketsByEventId(1);
        verify(ticketService).getTicketsByEventId(2);
        verify(cloudinaryService).generateImageUrl("1");
        verify(cloudinaryService).generateImageUrl("2");
    }

    @Test
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

        mockMvc.perform(get("/events/{id}", eventId))
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
                    """))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(eventMapper).toDomain(eventDTO);
        verify(eventService).createEvent(event);
    }

    @Test
    void createEvent_shouldNotCreateAndReturn400_WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {
                            "name": "",
                            "location": "",
                            "dateTime": ""
                        }
                    """))
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
                    """))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(eventMapper).toDomain(eventDTO);
        verify(eventService).updateEvent(eventId, event);
    }

    @Test
    void deleteEvent_shouldDeleteEventAndReturn204() throws Exception {
        int eventId = 1;

        mockMvc.perform(delete("/events/{id}", eventId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(eventId);
    }

    @Test
    void deleteEvent_shouldReturn404WhenEventNotFound() throws Exception {
        int eventId = 999;
        doThrow(new RuntimeException("Event not found")).when(eventService).deleteEvent(eventId);

        mockMvc.perform(delete("/events/{id}", eventId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));

        verify(eventService).deleteEvent(eventId);
    }
}
