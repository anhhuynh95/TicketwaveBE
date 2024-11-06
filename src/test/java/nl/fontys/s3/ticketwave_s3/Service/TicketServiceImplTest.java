package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
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
import java.util.Optional;

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
    private EventEntity sampleEventEntity;
    private TicketEntity sampleTicketEntity;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 10);
        sampleTicket = new Ticket(1, 1, "VIP", 60.0, 5); // assuming initial quantity of 5
        sampleEventEntity = new EventEntity(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 10, null);
        sampleTicketEntity = new TicketEntity(1, sampleEventEntity, "VIP", 60.0, 5);
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
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicket));

        Ticket ticket = ticketServiceImpl.getTicketById(sampleTicket.getId());

        assertNotNull(ticket);
        assertEquals("VIP", ticket.getTicketName());
        verify(ticketRepository).findById(sampleTicket.getId());
    }

    @Test
    void getTicketById_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.getTicketById(999));

        assertEquals("Ticket not found.", exception.getReason());
        verify(ticketRepository).findById(999);
    }

    @Test
    void createTicket_shouldAddNewTicket_whenEventExists() {
        when(eventRepository.findById(sampleEvent.getId())).thenReturn(sampleEvent);
        when(eventMapper.toEntity(sampleEvent)).thenReturn(sampleEventEntity);

        ticketServiceImpl.createTicket(sampleTicket);

        verify(ticketRepository).save(sampleTicket, sampleEventEntity);
    }

    @Test
    void createTicket_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.createTicket(sampleTicket));

        assertEquals("Event not found.", exception.getReason());
        verify(ticketRepository, never()).save(any(Ticket.class), any(EventEntity.class));
    }

//    @Test
//    void purchaseTicket_shouldDecreaseEventAndTicketQuantity_whenEnoughTicketsAvailable() {
//        when(ticketRepository.findEntityById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicketEntity));
//        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
//
//        ticketServiceImpl.purchaseTicket(sampleTicket.getId(), 3);
//
//        assertEquals(7, sampleEvent.getTicketQuantity());
//        assertEquals(2, sampleTicketEntity.getQuantity()); // Assuming the initial quantity was 5 and 3 were purchased
//        verify(eventRepository).save(sampleEvent);
//        verify(ticketRepository).saveEntity(sampleTicketEntity);
//    }
//
//    @Test
//    void purchaseTicket_shouldThrowException_whenNotEnoughTicketsAvailable() {
//        when(ticketRepository.findEntityById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicketEntity));
//        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
//
//        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketServiceImpl.purchaseTicket(sampleTicket.getId(), 15));
//
//        assertEquals("Not enough tickets available.", exception.getReason());
//        verify(eventRepository, never()).save(sampleEvent);
//        verify(ticketRepository, never()).saveEntity(any(TicketEntity.class));
//    }
//
//    @Test
//    void cancelTickets_shouldRestoreQuantities_whenPartialCancellation() {
//        when(ticketRepository.findEntityById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicketEntity));
//        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
//
//        PurchasedTicketEntity purchasedTicketEntity = new PurchasedTicketEntity(1, sampleTicketEntity, 5, LocalDateTime.now());
//        when(purchasedTicketRepository.findByTicketId(sampleTicket.getId())).thenReturn(List.of(purchasedTicketEntity));
//
//        ticketServiceImpl.cancelTickets(sampleTicket.getId(), 3);
//
//        assertEquals(13, sampleEvent.getTicketQuantity()); // 10 initial + 3 canceled
//        assertEquals(8, sampleTicketEntity.getQuantity()); // 5 initial + 3 canceled
//        assertEquals(2, purchasedTicketEntity.getPurchaseQuantity()); // Reduced by 3
//        verify(eventRepository).save(sampleEvent);
//        verify(ticketRepository).saveEntity(sampleTicketEntity);
//        verify(purchasedTicketRepository).save(purchasedTicketEntity);
//    }
//
//    @Test
//    void cancelTickets_shouldDeletePurchaseRecord_whenFullCancellation() {
//        when(ticketRepository.findEntityById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicketEntity));
//        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
//
//        PurchasedTicketEntity purchasedTicketEntity = new PurchasedTicketEntity(1, sampleTicketEntity, 5, LocalDateTime.now());
//        when(purchasedTicketRepository.findByTicketId(sampleTicket.getId())).thenReturn(List.of(purchasedTicketEntity));
//
//        ticketServiceImpl.cancelTickets(sampleTicket.getId(), 5);
//
//        assertEquals(15, sampleEvent.getTicketQuantity()); // 10 initial + 5 fully canceled
//        assertEquals(10, sampleTicketEntity.getQuantity()); // 5 initial + 5 fully canceled
//        verify(eventRepository).save(sampleEvent);
//        verify(ticketRepository).saveEntity(sampleTicketEntity);
//        verify(purchasedTicketRepository).delete(purchasedTicketEntity);
//    }
//
//    @Test
//    void cancelTickets_shouldThrowException_whenCancelQuantityExceedsPurchase() {
//        when(ticketRepository.findEntityById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicketEntity));
//        when(eventRepository.findById(sampleTicket.getEventId())).thenReturn(sampleEvent);
//
//        PurchasedTicketEntity purchasedTicketEntity = new PurchasedTicketEntity(1, sampleTicketEntity, 5, LocalDateTime.now());
//        when(purchasedTicketRepository.findByTicketId(sampleTicket.getId())).thenReturn(List.of(purchasedTicketEntity));
//
//        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
//                () -> ticketServiceImpl.cancelTickets(sampleTicket.getId(), 6));
//
//        assertEquals("Cannot cancel more tickets than purchased.", exception.getReason());
//        verify(purchasedTicketRepository, never()).delete(purchasedTicketEntity);
//        verify(purchasedTicketRepository, never()).save(any(PurchasedTicketEntity.class));
//    }

    @Test
    void deleteTicket_shouldRemoveTicket_whenTicketExists() {
        when(ticketRepository.findById(sampleTicket.getId())).thenReturn(Optional.of(sampleTicket));

        ticketServiceImpl.deleteTicket(sampleTicket.getId());

        verify(ticketRepository).deleteById(sampleTicket.getId());
    }

    @Test
    void deleteTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

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
