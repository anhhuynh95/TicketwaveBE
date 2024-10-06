package nl.fontys.s3.ticketwave_s3;

import nl.fontys.s3.ticketwave_s3.interfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.interfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.models.Event;
import nl.fontys.s3.ticketwave_s3.models.Ticket;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Control the order of execution
class TicketwaveApplicationTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventService eventService; // Ensure this is correctly annotated

    // Ticket Tests
    @Test
    @Order(1)
    void createTicket_shouldAddNewTicket() {
        Ticket newTicket = new Ticket(null, "Dance Concert", "Rotterdam", 60.0, 1);
        ticketService.createTicket(newTicket);

        List<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(4, tickets.size(), "The number of tickets should be 4.");
        Ticket addedTicket = tickets.get(3);
        assertEquals("Dance Concert", addedTicket.getEventName());
        assertEquals("Rotterdam", addedTicket.getLocation());
        assertEquals(60.0, addedTicket.getPrice());
    }

    @Test
    @Order(2)
    void createTicket_shouldThrowException_whenPriceIsNegative() {
        Ticket newTicket = new Ticket(null, "Invalid Ticket", "Location", -10.0, 1); // Negative price

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(newTicket);
        });

        assertEquals("Ticket price cannot be negative.", exception.getMessage());
    }

    @Test
    @Order(3)
    void createTicket_shouldThrowException_whenEventIdDoesNotExist() {
        Ticket newTicket = new Ticket(null, "Invalid Ticket", "Location", 20.0, 999); // Non-existent event ID

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(newTicket);
        });

        assertEquals("Event with ID 999 does not exist.", exception.getMessage());
    }

    @Test
    @Order(4)
    void getAllTickets_shouldReturnAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(4, tickets.size(), "There should be 4 tickets available.");
    }

    @Test
    @Order(5)
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        Ticket ticket = ticketService.getTicketById(1);
        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("Concert A", ticket.getEventName());
        assertEquals("Eindhoven", ticket.getLocation());
        assertEquals(50.0, ticket.getPrice());
        assertEquals(1, ticket.getEventId());
    }

    @Test
    @Order(6)
    void getTicketById_shouldReturnNull_whenTicketDoesNotExist() {
        Ticket ticket = ticketService.getTicketById(999);
        assertNull(ticket, "Ticket with ID 999 should not exist.");
    }

    @Test
    @Order(7)
    void getTicketsByPrice_shouldReturnTickets_whenPriceIsBelowMaxPrice() {
        List<Ticket> tickets = ticketService.getTicketsByPrice(50.0);
        assertEquals(2, tickets.size(), "There should be 2 tickets with a price of 50.0 or less.");
        assertEquals("Concert A", tickets.get(0).getEventName());
        assertEquals("Art Exhibition", tickets.get(1).getEventName());
    }

    @Test
    @Order(8)
    void updateTicket_shouldModifyExistingTicket() {
        Ticket updatedTicket = new Ticket(null, "Updated Event", "Updated Location", 100.0, 1);
        ticketService.updateTicket(1, updatedTicket);

        Ticket ticket = ticketService.getTicketById(1);
        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("Updated Event", ticket.getEventName());
        assertEquals("Updated Location", ticket.getLocation());
        assertEquals(100.0, ticket.getPrice());
        assertEquals(1, ticket.getEventId());
    }

    @Test
    @Order(9)
    void updateTicket_shouldThrowException_whenTicketDoesNotExist() {
        Ticket updatedTicket = new Ticket(null, "Updated Event", "Updated Location", 100.0, 1);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            ticketService.updateTicket(999, updatedTicket); // Invalid ID
        });

        assertEquals("Ticket not found.", ((ResponseStatusException) exception).getReason());
    }

    @Test
    @Order(10)
    void deleteTicket_shouldRemoveExistingTicket() {
        int initialSize = ticketService.getAllTickets().size();
        ticketService.deleteTicket(1);
        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(initialSize - 1, tickets.size(), "The number of tickets should decrease by 1.");
        Ticket deletedTicket = ticketService.getTicketById(1);
        assertNull(deletedTicket, "Deleted ticket with ID 1 should not exist.");
    }

    @Test
    @Order(11)
    void deleteTicket_shouldThrowException_whenTicketDoesNotExist() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            ticketService.deleteTicket(999); // Invalid ID
        });

        assertEquals("Ticket not found.", ((ResponseStatusException) exception).getReason());
    }

    // Event Tests
    @Test
    @Order(12)
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
    @Order(13)
    void getAllEvents_shouldReturnAllEvents() {
        List<Event> events = eventService.getAllEvents();
        assertEquals(4, events.size(), "There should be 4 events available.");
    }

    @Test
    @Order(14)
    void getEventById_shouldReturnEvent_whenEventExists() {
        Event event = eventService.getEventById(1);
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Concert A", event.getName());
    }

    @Test
    @Order(15)
    void getEventById_shouldReturnNull_whenEventDoesNotExist() {
        Event event = eventService.getEventById(999);
        assertNull(event, "Event with ID 999 should not exist.");
    }

    @Test
    @Order(16)
    void updateEvent_shouldModifyExistingEvent() {
        Event updatedEvent = new Event(null, "Updated Event", "Updated Location", "Updated Description", "2024-09-15T20:00");
        eventService.updateEvent(1, updatedEvent);

        Event event = eventService.getEventById(1);
        assertNotNull(event, "Event with ID 1 should exist.");
        assertEquals("Updated Event", event.getName());
    }

    @Test
    @Order(17)
    void deleteEvent_shouldRemoveExistingEvent() {
        int initialSize = eventService.getAllEvents().size();
        eventService.deleteEvent(1);
        List<Event> events = eventService.getAllEvents();

        assertEquals(initialSize - 1, events.size(), "The number of events should decrease by 1.");
    }

    @Test
    @Order(18)
    void deleteEvent_shouldThrowException_whenEventDoesNotExist() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            eventService.deleteEvent(999); // Invalid ID
        });

        assertEquals("Event not found.", ((ResponseStatusException) exception).getReason());
    }
}
