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

        Ticket newTicket = new Ticket(null, "New Event", "Rotterdam", 60.0);
        ticketService.createTicket(newTicket);
        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(initialSize + 1, tickets.size());
        Ticket addedTicket = tickets.get(tickets.size() - 1);
        assertEquals("New Event", addedTicket.getEventName());
        assertEquals("Rotterdam", addedTicket.getLocation());
        assertEquals(60.0, addedTicket.getPrice());
    }

    @Test
    void getAllTickets_shouldReturnAllInitialTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(4, tickets.size(), "There should be 4 tickets available initially.");
    }

    @Test
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        Ticket ticket = ticketService.getTicketById(1);

        assertEquals("Concert A", ticket.getEventName());
        assertEquals("Eindhoven", ticket.getLocation());
        assertEquals(50.0, ticket.getPrice());
    }

    @Test
    void getTicketById_shouldReturnNull_whenTicketDoesNotExist() {
        Ticket ticket = ticketService.getTicketById(999);

        assertNull(ticket, "Ticket with ID 999 should not exist.");
    }

    @Test
    void getTicketsByPrice_shouldReturnTickets_whenPriceIsBelowMaxPrice() {
        List<Ticket> tickets = ticketService.getTicketsByPrice(50.0);

        assertEquals(2, tickets.size());
        assertEquals("Concert A", tickets.get(0).getEventName());
        assertEquals("Art Exhibition", tickets.get(1).getEventName());
    }
}

