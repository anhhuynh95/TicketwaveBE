package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private TicketServiceImpl ticketServiceImpl;

    private Event sampleEvent;
    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 10);
        sampleTicket = new Ticket(1, 1, "VIP", 60.0, 1);
    }

    @Test
    void getAllTickets_shouldReturnAllTickets() {
        when(ticketRepository.findAll()).thenReturn(List.of(sampleTicket));

        List<Ticket> tickets = ticketServiceImpl.getAllTickets();

        assertEquals(1, tickets.size());
        assertEquals("VIP", tickets.get(0).getTicketName());
        verify(ticketRepository).findAll();
    }

    @Test
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(sampleTicket);

        Ticket ticket = ticketServiceImpl.getTicketById(sampleTicket.getId());

        assertNotNull(ticket);
        assertEquals("VIP", ticket.getTicketName());
        verify(ticketRepository).findById(sampleTicket.getId());
    }

    @Test
    void getTicketById_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(999)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.getTicketById(999));

        assertEquals("Ticket not found.", exception.getReason());
        verify(ticketRepository).findById(999);
    }

    @Test
    void createTicket_shouldAddNewTicket_whenEventExists() {
        when(eventRepository.findById(sampleEvent.getId())).thenReturn(sampleEvent);
        EventEntity eventEntity = new EventEntity(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 10, null);
        when(eventMapper.toEntity(sampleEvent)).thenReturn(eventEntity);

        ticketServiceImpl.createTicket(sampleTicket);

        verify(ticketRepository).save(sampleTicket, eventEntity);
    }

    @Test
    void createTicket_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.createTicket(sampleTicket));

        assertEquals("Event not found.", exception.getReason());
        verify(ticketRepository, never()).save(any(Ticket.class), any(EventEntity.class));
    }

    @Test
    void purchaseTicket_shouldDecreaseEventTicketQuantity_whenEnoughTicketsAvailable() {
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(sampleTicket);
        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
        EventEntity eventEntity = new EventEntity(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 10, null);
        when(eventMapper.toEntity(sampleEvent)).thenReturn(eventEntity);

        ticketServiceImpl.purchaseTicket(sampleTicket.getId(), 5);

        assertEquals(5, sampleEvent.getTicketQuantity());
        verify(eventRepository).save(sampleEvent);
        verify(ticketRepository).save(sampleTicket, eventEntity);
    }

    @Test
    void purchaseTicket_shouldThrowException_whenNotEnoughTicketsAvailable() {
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(sampleTicket);
        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.purchaseTicket(sampleTicket.getId(), 15));

        assertEquals("Not enough tickets available.", exception.getReason());
        verify(eventRepository, never()).save(sampleEvent);
        verify(ticketRepository, never()).save(any(Ticket.class), any(EventEntity.class));
    }

    @Test
    void deleteTicket_shouldRemoveTicket_whenTicketExists() {
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(sampleTicket);

        ticketServiceImpl.deleteTicket(sampleTicket.getId());

        verify(ticketRepository).deleteById(sampleTicket.getId());
    }

    @Test
    void deleteTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(999)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.deleteTicket(999));

        assertEquals("Ticket not found.", exception.getReason());
        verify(ticketRepository, never()).deleteById(999);
    }

    @Test
    void getTicketsByPrice_shouldReturnTicketsBelowMaxPrice() {
        when(ticketRepository.findAll()).thenReturn(List.of(sampleTicket));

        List<Ticket> tickets = ticketServiceImpl.getTicketsByPrice(100.0);

        assertEquals(1, tickets.size());
        assertTrue(tickets.stream().allMatch(t -> t.getPrice() <= 100.0));
        verify(ticketRepository).findAll();
    }
}
