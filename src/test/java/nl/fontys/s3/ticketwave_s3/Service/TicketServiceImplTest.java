package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.PurchasedTicketRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private PurchasedTicketRepository purchasedTicketRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void getAllTickets_shouldReturnAllTickets() {
        List<Ticket> tickets = List.of(
                Ticket.builder().id(1).ticketName("VIP").price(100.0).build(),
                Ticket.builder().id(2).ticketName("Standard").price(50.0).build()
        );

        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertEquals(tickets, result);
        verify(ticketRepository).findAll();
    }

    @Test
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        Ticket ticket = Ticket.builder().id(1).ticketName("VIP").price(100.0).build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(1);

        assertEquals(ticket, result);
        verify(ticketRepository).findById(1);
    }

    @Test
    void getTicketById_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResponseStatusException.class, () -> ticketService.getTicketById(1));
        assertEquals("404 NOT_FOUND \"Ticket not found.\"", exception.getMessage());
        verify(ticketRepository).findById(1);
    }

    @Test
    void createTicket_shouldSaveTicket_whenEventExists() {
        Event event = Event.builder().id(1).ticketQuantity(200).build();
        Ticket ticket = Ticket.builder().id(1).eventId(1).quantity(50).build();
        EventEntity eventEntity = EventEntity.builder().id(1).ticketQuantity(200).build();

        when(eventRepository.findById(1)).thenReturn(event);
        when(eventMapper.toEntity(event)).thenReturn(eventEntity);

        ticketService.createTicket(ticket);

        verify(ticketRepository).save(ticket, eventEntity);
    }

    @Test
    void createTicket_shouldThrowException_whenEventNotFound() {
        Ticket ticket = Ticket.builder().id(1).eventId(1).build();

        when(eventRepository.findById(1)).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> ticketService.createTicket(ticket));
        assertEquals("404 NOT_FOUND \"Event not found.\"", exception.getMessage());
    }

    @Test
    void deleteTicket_shouldRemoveTicket_whenTicketExists() {
        Ticket ticket = Ticket.builder().id(1).build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(1);

        verify(ticketRepository).deleteById(1);
    }

    @Test
    void deleteTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResponseStatusException.class, () -> ticketService.deleteTicket(1));
        assertEquals("404 NOT_FOUND \"Ticket not found.\"", exception.getMessage());
    }

    @Test
    void purchaseTicket_shouldReduceTicketAndEventQuantity() {
        Ticket ticket = Ticket.builder().id(1).quantity(50).eventId(1).build();
        Event event = Event.builder().id(1).ticketQuantity(100).build();
        TicketEntity ticketEntity = TicketEntity.builder().id(1).quantity(50).build();
        UserEntity userEntity = UserEntity.builder().id(1).build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findEntityById(1)).thenReturn(Optional.of(ticketEntity));
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));

        ticketService.purchaseTicket(1, 10, 1);

        assertEquals(40, ticketEntity.getQuantity());
        assertEquals(90, event.getTicketQuantity());
        verify(ticketRepository).saveEntity(ticketEntity);
        verify(eventRepository).save(event);
    }

    @Test
    void purchaseTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResponseStatusException.class, () -> ticketService.purchaseTicket(1, 10, 1));
        assertEquals("404 NOT_FOUND \"Ticket not found.\"", exception.getMessage());
    }

    @Test
    void cancelTickets_shouldRestoreTicketAndEventQuantity() {
        // Arrange
        Ticket ticket = Ticket.builder().id(1).eventId(1).build();
        Event event = Event.builder().id(1).ticketQuantity(90).build();
        TicketEntity ticketEntity = TicketEntity.builder().id(1).quantity(10).build();
        PurchasedTicketEntity purchasedTicket = PurchasedTicketEntity.builder()
                .ticket(ticketEntity)
                .purchaseQuantity(10)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findEntityById(1)).thenReturn(Optional.of(ticketEntity));
        when(purchasedTicketRepository.findByTicketId(1)).thenReturn(List.of(purchasedTicket));

        // Act
        ticketService.cancelTickets(1, 5);

        // Assert
        assertEquals(95, event.getTicketQuantity());
        assertEquals(15, ticketEntity.getQuantity()); // Quantity restored by 5
        verify(eventRepository).save(event);
        verify(ticketRepository).saveEntity(ticketEntity);
    }

    @Test
    void getPurchasedTickets_shouldReturnAllPurchasedTickets() {
        PurchasedTicketEntity purchasedTicket = PurchasedTicketEntity.builder()
                .ticket(TicketEntity.builder().id(1).build())
                .purchaseQuantity(10)
                .build();
        PurchasedTicketDTO dto = PurchasedTicketDTO.builder().quantity(10).build();

        when(purchasedTicketRepository.findByUserId(1)).thenReturn(List.of(purchasedTicket));
        when(ticketMapper.toPurchasedTicketDTO(purchasedTicket)).thenReturn(dto);

        List<PurchasedTicketDTO> result = ticketService.getPurchasedTickets(1);

        assertEquals(1, result.size());
        verify(purchasedTicketRepository).findByUserId(1);
    }
    @Test
    void getTicketsByPrice_shouldReturnFilteredTickets() {
        // Arrange
        List<Ticket> tickets = List.of(
                Ticket.builder().id(1).price(100.0).build(),
                Ticket.builder().id(2).price(50.0).build()
        );
        when(ticketRepository.findAll()).thenReturn(tickets);

        // Act
        List<Ticket> result = ticketService.getTicketsByPrice(80.0);

        // Assert
        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getPrice());
        verify(ticketRepository).findAll();
    }

    @Test
    void updateTicket_shouldUpdateTicketSuccessfully() {
        // Arrange
        Integer ticketId = 1;
        Ticket existingTicket = Ticket.builder().id(ticketId).eventId(1).build();
        Ticket updatedTicket = Ticket.builder().id(ticketId).eventId(1).quantity(20).build();
        Event event = Event.builder().id(1).build();
        EventEntity eventEntity = EventEntity.builder().id(1).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(eventMapper.toEntity(event)).thenReturn(eventEntity);

        // Act
        ticketService.updateTicket(ticketId, updatedTicket);

        // Assert
        verify(ticketRepository).save(updatedTicket, eventEntity);
    }

    @Test
    void updateTicket_shouldThrowException_whenTicketNotFound() {
        // Arrange
        Integer ticketId = 1;
        Ticket updatedTicket = Ticket.builder().id(ticketId).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.updateTicket(ticketId, updatedTicket));
        assertEquals("404 NOT_FOUND \"Ticket not found.\"", exception.getMessage());
    }

    @Test
    void updateTicket_shouldThrowException_whenEventNotFound() {
        // Arrange
        Integer ticketId = 1;
        Ticket existingTicket = Ticket.builder().id(ticketId).eventId(1).build();
        Ticket updatedTicket = Ticket.builder().id(ticketId).eventId(1).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(eventRepository.findById(1)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.updateTicket(ticketId, updatedTicket));
        assertEquals("404 NOT_FOUND \"Event not found.\"", exception.getMessage());
    }

    @Test
    void purchaseTicket_shouldThrowException_whenEventNotFound() {
        // Arrange
        Integer ticketId = 1;
        Integer userId = 1;
        Integer quantity = 10;
        Ticket ticket = Ticket.builder().id(ticketId).eventId(2).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(2)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.purchaseTicket(ticketId, quantity, userId));
        assertEquals("404 NOT_FOUND \"Event not found.\"", exception.getMessage());
    }

    @Test
    void cancelTickets_shouldThrowException_whenPurchasedTicketsNotFound() {
        // Arrange
        Integer ticketId = 1;
        Ticket ticket = Ticket.builder().id(ticketId).eventId(1).build();
        Event event = Event.builder().id(1).ticketQuantity(50).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(purchasedTicketRepository.findByTicketId(ticketId)).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.cancelTickets(ticketId, 5));
        assertEquals("404 NOT_FOUND \"Purchased ticket record not found.\"", exception.getMessage());
    }

    @Test
    void cancelTickets_shouldThrowException_whenCancelQuantityExceedsPurchasedQuantity() {
        // Arrange
        Integer ticketId = 1;
        Ticket ticket = Ticket.builder().id(ticketId).eventId(1).build();
        Event event = Event.builder().id(1).ticketQuantity(50).build();
        PurchasedTicketEntity purchasedTicket = PurchasedTicketEntity.builder().purchaseQuantity(5).build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(purchasedTicketRepository.findByTicketId(ticketId)).thenReturn(List.of(purchasedTicket));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.cancelTickets(ticketId, 10));
        assertEquals("400 BAD_REQUEST \"Cannot cancel more tickets than purchased.\"", exception.getMessage());
    }

    @Test
    void getTotalPurchasesByEventType_shouldReturnCorrectMapping() {
        // Arrange
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> results = List.of(new Object[]{EventType.MUSIC, 100L}, new Object[]{EventType.SPORTS, 50L});

        when(purchasedTicketRepository.findPurchasesByEventType(sixMonthsAgo)).thenReturn(results);

        // Act
        Map<String, Long> result = ticketService.getTotalPurchasesByEventType();

        // Assert
        assertEquals(2, result.size());
        assertEquals(100L, result.get("MUSIC"));
        assertEquals(50L, result.get("SPORTS"));
    }

    @Test
    void getMonthlySales_shouldReturnCorrectMapping() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        List<Object[]> results = List.of(
                new Object[]{2023, 1, 1000.0},
                new Object[]{2023, 2, 2000.0}
        );

        when(purchasedTicketRepository.findMonthlySales(startDate)).thenReturn(results);

        // Act
        Map<String, Double> result = ticketService.getMonthlySales(startDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1000.0, result.get("January 2023"));
        assertEquals(2000.0, result.get("February 2023"));
    }
}
