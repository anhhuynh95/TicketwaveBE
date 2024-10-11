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

    @Test
    @Order(1)
    void createEvent_shouldAddNewEvent() {
        // Arrange
        Event newEvent = new Event(null, "Dance Concert", "Rotterdam", "An exciting dance concert.", "2024-09-01T20:00", 75);

        // Act
        eventService.createEvent(newEvent);
        List<Event> events = eventService.getAllEvents();

        // Assert
        assertEquals(4, events.size(), "The number of events should be 4.");
        Event addedEvent = events.get(3);
        assertEquals("Dance Concert", addedEvent.getName());
        assertEquals("Rotterdam", addedEvent.getLocation());
    }

    @Test
    @Order(2)
    void getAllEvents_shouldReturnAllEvents() {
        // Act
        List<Event> events = eventService.getAllEvents();

        // Assert
        assertEquals(3, events.size(), "There should be 3 events initially.");
    }

    @Test
    @Order(3)
    void getEventById_shouldReturnEvent_whenEventExists() {
        // Act
        Event event = eventService.getEventById(1);

        // Assert
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Concert A", event.getName());
    }

    @Test
    @Order(4)
    void getEventById_shouldReturnNull_whenEventDoesNotExist() {
        // Act
        Event event = eventService.getEventById(999);

        // Assert
        assertNull(event, "Event with ID 999 should not exist.");
    }

    @Test
    @Order(5)
    void updateEvent_shouldModifyExistingEvent() {
        // Arrange
        Event updatedEvent = new Event(null, "Updated Event", "Updated Location", "Updated Description", "2024-09-15T20:00", 50);

        // Act
        eventService.updateEvent(1, updatedEvent);
        Event event = eventService.getEventById(1);

        // Assert
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Updated Event", event.getName());
    }

    @Test
    @Order(6)
    void deleteEvent_shouldRemoveExistingEvent() {
        // Arrange
        int initialSize = eventService.getAllEvents().size();

        // Act
        eventService.deleteEvent(1);
        List<Event> events = eventService.getAllEvents();

        // Assert
        assertEquals(initialSize - 1, events.size(), "The number of events should decrease by 1.");
    }

    @Test
    @Order(7)
    void deleteEvent_shouldThrowException_whenEventDoesNotExist() {
        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            eventService.deleteEvent(999); // Invalid ID
        });

        // Assert
        assertEquals("Event not found.", exception.getReason());
    }
}
