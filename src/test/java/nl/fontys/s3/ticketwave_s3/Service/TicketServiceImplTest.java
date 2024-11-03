package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.EventService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TicketServiceImplTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventService eventService;

    @BeforeEach
    void setUp() {
        // Set up required events for tickets
        eventService.createEvent(new Event(null, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 2));
        eventService.createEvent(new Event(null, "Art Exhibition", "Nuenen", "A stunning art exhibition", "2024-09-05T18:00", 2));
        eventService.createEvent(new Event(null, "Sports Event", "Amsterdam", "An amazing football match", "2024-09-10T21:00", 2));
    }

    @Test
    @Order(1)
    void createTicket_shouldAddNewTicket() {
        // Arrange
        Ticket newTicket = new Ticket(null, 1, "VIP", 60.0, 1);

        // Act
        ticketService.createTicket(newTicket);
        List<Ticket> tickets = ticketService.getAllTickets();

        // Assert
        assertEquals(4, tickets.size(), "The number of tickets should be 4.");
        Ticket addedTicket = tickets.get(3);
        assertEquals("VIP", addedTicket.getTicketName());
        assertEquals(60.0, addedTicket.getPrice());
        assertEquals(1, addedTicket.getEventId());
    }

    @Test
    @Order(2)
    void createTicket_shouldThrowException_whenPriceIsNegative() {
        // Arrange
        Ticket newTicket = new Ticket(null, 1, "VIP", -10.0, 1);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(newTicket));
        assertEquals("Ticket price cannot be negative.", exception.getMessage());
    }

    @Test
    @Order(3)
    void createTicket_shouldThrowException_whenEventIdDoesNotExist() {
        // Arrange
        Ticket newTicket = new Ticket(null, 999, "VIP", 20.0, 1);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(newTicket));
        assertEquals("Event with ID 999 does not exist.", exception.getMessage());
    }

    @Test
    @Order(4)
    void getAllTickets_shouldReturnAllTickets() {
        // Act
        List<Ticket> tickets = ticketService.getAllTickets();

        // Assert
        assertEquals(3, tickets.size(), "There should be 3 tickets available.");
    }

    @Test
    @Order(5)
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        // Act
        Ticket ticket = ticketService.getTicketById(1);

        // Assert
        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("VIP", ticket.getTicketName());
        assertEquals(50.0, ticket.getPrice());
        assertEquals(1, ticket.getEventId());
    }

    @Test
    @Order(6)
    void getTicketById_shouldReturnNull_whenTicketDoesNotExist() {
        // Act
        Ticket ticket = ticketService.getTicketById(999);

        // Assert
        assertNull(ticket, "Ticket with ID 999 should not exist.");
    }

    @Test
    @Order(7)
    void getTicketsByPrice_shouldReturnTickets_whenPriceIsBelowMaxPrice() {
        // Act
        List<Ticket> tickets = ticketService.getTicketsByPrice(50.0);

        // Assert
        assertEquals(2, tickets.size(), "There should be 2 tickets with a price of 50.0 or less.");
        assertEquals("Concert A", tickets.get(0).getTicketName());
        assertEquals("Art Exhibition", tickets.get(1).getTicketName());
    }

    @Test
    @Order(8)
    void updateTicket_shouldModifyExistingTicket() {
        // Arrange
        Ticket updatedTicket = new Ticket(null, 1, "Updated VIP", 100.0, 1);

        // Act
        ticketService.updateTicket(1, updatedTicket);

        // Assert
        Ticket ticket = ticketService.getTicketById(1);
        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("Updated VIP", ticket.getTicketName());
        assertEquals(100.0, ticket.getPrice());
    }

    @Test
    @Order(9)
    void updateTicket_shouldThrowException_whenTicketDoesNotExist() {
        // Arrange
        Ticket updatedTicket = new Ticket(null, 1, "Updated VIP", 100.0, 1);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ticketService.updateTicket(999, updatedTicket);
        });
        assertEquals("Ticket not found.", exception.getReason());
    }

    @Test
    @Order(10)
    void deleteTicket_shouldRemoveExistingTicket() {
        // Arrange
        int initialSize = ticketService.getAllTickets().size();

        // Act
        ticketService.deleteTicket(1);
        List<Ticket> tickets = ticketService.getAllTickets();

        // Assert
        assertEquals(initialSize - 1, tickets.size(), "The number of tickets should decrease by 1.");
        Ticket deletedTicket = ticketService.getTicketById(1);
        assertNull(deletedTicket, "Deleted ticket with ID 1 should not exist.");
    }

    @Test
    @Order(11)
    void deleteTicket_shouldThrowException_whenTicketDoesNotExist() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ticketService.deleteTicket(999);
        });
        assertEquals("Ticket not found.", exception.getReason());
    }
}
