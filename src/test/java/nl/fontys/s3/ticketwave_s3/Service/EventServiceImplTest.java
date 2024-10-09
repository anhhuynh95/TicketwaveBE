package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Control the order of execution
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Reset context after each test
class EventServiceImplTest {

    @Autowired
    private EventService eventService;


//    @BeforeEach
//    void setUp() {
//
//        // Ensure the necessary events are set up for testing event-related features
//        eventService.createEvent(new Event(null, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00"));
//        eventService.createEvent(new Event(null, "Art Exhibition", "Nuenen", "A stunning art exhibition", "2024-09-05T18:00"));
//        eventService.createEvent(new Event(null, "Sports Event", "Amsterdam", "An amazing football match", "2024-09-10T21:00"));
//    }

    @Test
    @Order(1)
    void createEvent_shouldAddNewEvent() {
        Event newEvent = new Event(null, "Dance Concert", "Rotterdam", "An exciting dance concert.", "2024-09-01T20:00");
        eventService.createEvent(newEvent);

        List<Event> events = eventService.getAllEvents();
        assertEquals(4, events.size(), "The number of events should be 4.");
        Event addedEvent = events.get(3);
        assertEquals("Dance Concert", addedEvent.getName());
        assertEquals("Rotterdam", addedEvent.getLocation());
    }

    @Test
    @Order(2)
    void getAllEvents_shouldReturnAllEvents() {
        List<Event> events = eventService.getAllEvents();
        assertEquals(3, events.size(), "There should be 4 events available.");
    }

    @Test
    @Order(3)
    void getEventById_shouldReturnEvent_whenEventExists() {
        Event event = eventService.getEventById(1);
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Concert A", event.getName());
    }

    @Test
    @Order(4)
    void getEventById_shouldReturnNull_whenEventDoesNotExist() {
        Event event = eventService.getEventById(999);
        assertNull(event, "Event with ID 999 should not exist.");
    }

    @Test
    @Order(5)
    void updateEvent_shouldModifyExistingEvent() {
        Event updatedEvent = new Event(null, "Updated Event", "Updated Location", "Updated Description", "2024-09-15T20:00");
        eventService.updateEvent(1, updatedEvent);

        Event event = eventService.getEventById(1);
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Updated Event", event.getName());
    }

    @Test
    @Order(6)
    void deleteEvent_shouldRemoveExistingEvent() {
        int initialSize = eventService.getAllEvents().size();
        eventService.deleteEvent(1);
        List<Event> events = eventService.getAllEvents();

        assertEquals(initialSize - 1, events.size(), "The number of events should decrease by 1.");
    }

    @Test
    @Order(7)
    void deleteEvent_shouldThrowException_whenEventDoesNotExist() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            eventService.deleteEvent(999); // Invalid ID
        });

        assertEquals("Event not found.", exception.getReason());
    }
}