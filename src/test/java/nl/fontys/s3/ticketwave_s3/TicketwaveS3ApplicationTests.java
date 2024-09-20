package nl.fontys.s3.ticketwave_s3;

import nl.fontys.s3.ticketwave_s3.models.Ticket;
import nl.fontys.s3.ticketwave_s3.business.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketwaveApplicationTests {

    @Autowired
    private TicketService ticketService;

    @Test
    void contextLoads() {
    }

    @Test
    void createTicket_shouldAddNewTicket() {

        int initialSize = ticketService.getAllTickets().size();
        Ticket newTicket = new Ticket(null, "Dance Concert", "Rotterdam", 60.0);

        ticketService.createTicket(newTicket);
        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(initialSize + 1, tickets.size(), "The number of tickets should increase by 1.");
        Ticket addedTicket = tickets.get(tickets.size() - 1);
        assertEquals("Dance Concert", addedTicket.getEventName(), "The event name should be 'Dance Concert'.");
        assertEquals("Rotterdam", addedTicket.getLocation(), "The location should be 'Rotterdam'.");
        assertEquals(60.0, addedTicket.getPrice(), "The price should be 60.0.");
    }

    @Test
    void getAllTickets_shouldReturnAllTickets() {

        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(3, tickets.size(), "There should be 3 tickets available.");
    }

    @Test
    void getTicketById_shouldReturnTicket_whenTicketExists() {

        Ticket ticket = ticketService.getTicketById(1);

        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("Concert A", ticket.getEventName(), "The event name should be 'Concert A'.");
        assertEquals("Eindhoven", ticket.getLocation(), "The location should be 'Eindhoven'.");
        assertEquals(50.0, ticket.getPrice(), "The price should be 50.0.");
    }

    @Test
    void getTicketById_shouldReturnNull_whenTicketDoesNotExist() {

        Ticket ticket = ticketService.getTicketById(999);

        assertNull(ticket, "Ticket with ID 999 should not exist.");
    }

    @Test
    void getTicketsByPrice_shouldReturnTickets_whenPriceIsBelowMaxPrice() {

        List<Ticket> tickets = ticketService.getTicketsByPrice(50.0);

        assertEquals(2, tickets.size(), "There should be 2 tickets with a price of 50.0 or less.");
        assertEquals("Concert A", tickets.get(0).getEventName(), "The first ticket should be 'Concert A'.");
        assertEquals("Art Exhibition", tickets.get(1).getEventName(), "The second ticket should be 'Art Exhibition'.");
    }

    @Test
    void updateTicket_shouldModifyExistingTicket() {

        Ticket updatedTicket = new Ticket(null, "Updated Event", "Updated Location", 100.0);

        ticketService.updateTicket(1, updatedTicket);
        Ticket ticket = ticketService.getTicketById(1);

        assertNotNull(ticket, "Ticket with ID 1 should exist.");
        assertEquals("Updated Event", ticket.getEventName(), "The event name should be 'Updated Event'.");
        assertEquals("Updated Location", ticket.getLocation(), "The location should be 'Updated Location'.");
        assertEquals(100.0, ticket.getPrice(), "The price should be 100.0.");
    }

    @Test
    void deleteTicket_shouldRemoveExistingTicket() {

        int initialSize = ticketService.getAllTickets().size();

        ticketService.deleteTicket(2);
        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(initialSize - 1, tickets.size(), "The number of tickets should decrease by 1.");
        Ticket deletedTicket = ticketService.getTicketById(2);
        assertNull(deletedTicket, "Deleted ticket with ID 1 should not exist.");
    }
}
