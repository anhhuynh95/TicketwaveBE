package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.PurchasedTicketRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found.";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found.";

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

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void getAllTickets_shouldReturnAllTickets() {
        List<Ticket> tickets = List.of(Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build());
        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertEquals(tickets, result);
        verify(ticketRepository).findAll();
    }

    @Test
    void getTicketById_shouldReturnTicket_whenTicketExists() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(1);

        assertEquals(ticket, result);
        verify(ticketRepository).findById(1);
    }

    @Test
    void getTicketById_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.getTicketById(1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(TICKET_NOT_FOUND_MESSAGE, exception.getReason());
        verify(ticketRepository).findById(1);
    }

    @Test
    void getTicketsByPrice_shouldReturnFilteredTickets() {
        Ticket ticket1 = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(50.0)
                .quantity(10)
                .build();
        Ticket ticket2 = Ticket.builder()
                .id(2)
                .eventId(2)
                .ticketName("Standard")
                .price(150.0)
                .quantity(20)
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(ticket1, ticket2));

        List<Ticket> result = ticketService.getTicketsByPrice(100.0);

        assertEquals(1, result.size());
        assertEquals(ticket1, result.get(0));
        verify(ticketRepository).findAll();
    }

    @Test
    void getTicketsByEventId_shouldReturnTicketsForEvent() {
        Ticket ticket1 = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(50.0)
                .quantity(10)
                .build();
        Ticket ticket2 = Ticket.builder()
                .id(2)
                .eventId(1)
                .ticketName("Standard")
                .price(150.0)
                .quantity(20)
                .build();

        when(ticketRepository.findByEventId(1)).thenReturn(List.of(ticket1, ticket2));

        List<Ticket> result = ticketService.getTicketsByEventId(1);

        assertEquals(2, result.size());
        assertEquals(ticket1, result.get(0));
        assertEquals(ticket2, result.get(1));
        verify(ticketRepository).findByEventId(1);
    }

    @Test
    void createTicket_shouldSaveTicket_whenValidEventExists() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .description("Great event")
                .dateTime("2024-12-01T18:00")
                .ticketQuantity(100)
                .build();
        EventEntity eventEntity = new EventEntity();

        when(eventRepository.findById(1)).thenReturn(event);
        when(eventMapper.toEntity(event)).thenReturn(eventEntity);

        ticketService.createTicket(ticket);

        verify(ticketRepository).save(ticket, eventEntity);
    }

    @Test
    void createTicket_shouldThrowException_whenEventNotFound() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(999)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        when(eventRepository.findById(ticket.getEventId())).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.createTicket(ticket));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(EVENT_NOT_FOUND_MESSAGE, exception.getReason());
        verify(eventRepository).findById(ticket.getEventId());
    }

    @Test
    void updateTicket_shouldUpdateTicket_whenValidEventExists() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .description("Great event")
                .dateTime("2024-12-01T18:00")
                .ticketQuantity(100)
                .build();
        EventEntity eventEntity = new EventEntity();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(eventMapper.toEntity(event)).thenReturn(eventEntity);

        ticketService.updateTicket(1, ticket);

        verify(ticketRepository).save(ticket, eventEntity);
    }

    @Test
    void updateTicket_shouldThrowException_whenEventNotFound() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(ticket.getEventId())).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.updateTicket(1, ticket));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(EVENT_NOT_FOUND_MESSAGE, exception.getReason());
    }

    @Test
    void deleteTicket_shouldDeleteTicket_whenTicketExists() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(1);

        verify(ticketRepository).deleteById(1);
    }

    @Test
    void deleteTicket_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.deleteTicket(1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(TICKET_NOT_FOUND_MESSAGE, exception.getReason());
    }

    @Test
    void purchaseTicket_shouldDeductQuantityAndSave_whenValidRequest() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .description("Great event")
                .dateTime("2024-12-01T18:00")
                .ticketQuantity(100)
                .build();
        TicketEntity ticketEntity = TicketEntity.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findEntityById(1)).thenReturn(Optional.of(ticketEntity));

        ticketService.purchaseTicket(1, 5);

        verify(eventRepository).save(event);
        verify(ticketRepository).saveEntity(ticketEntity);
    }

    @Test
    void purchaseTicket_shouldThrowException_whenQuantityExceedsAvailable() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .quantity(2)
                .build();
        Event event = Event.builder()
                .id(1)
                .ticketQuantity(1)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.purchaseTicket(1, 5));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Not enough tickets available.", exception.getReason());
    }

    @Test
    void purchaseTicket_shouldThrowException_whenTicketEntityNotFound() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .ticketQuantity(5)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findEntityById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.purchaseTicket(1, 2));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ticket entity not found.", exception.getReason());
    }

    @Test
    void cancelTickets_shouldRestoreQuantity_whenValidRequest() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .description("Great event")
                .dateTime("2024-12-01T18:00")
                .ticketQuantity(100)
                .build();
        TicketEntity ticketEntity = TicketEntity.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        PurchasedTicketEntity purchasedTicketEntity = PurchasedTicketEntity.builder()
                .id(1)
                .ticket(ticketEntity)
                .purchaseQuantity(5)
                .purchaseDate(LocalDateTime.now())
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findEntityById(1)).thenReturn(Optional.of(ticketEntity));
        when(purchasedTicketRepository.findByTicketId(1)).thenReturn(List.of(purchasedTicketEntity));

        ticketService.cancelTickets(1, 3);

        assertEquals(2, purchasedTicketEntity.getPurchaseQuantity());
        verify(eventRepository).save(event);
        verify(ticketRepository).saveEntity(ticketEntity);
    }

    @Test
    void cancelTickets_shouldThrowException_whenCancelQuantityExceedsPurchased() {
        Ticket ticket = Ticket.builder()
                .id(1)
                .eventId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        Event event = Event.builder()
                .id(1)
                .ticketQuantity(100)
                .build();
        PurchasedTicketEntity purchasedTicketEntity = PurchasedTicketEntity.builder()
                .id(1)
                .purchaseQuantity(2)
                .build();

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);
        when(purchasedTicketRepository.findByTicketId(1)).thenReturn(List.of(purchasedTicketEntity));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ticketService.cancelTickets(1, 5));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cannot cancel more tickets than purchased.", exception.getReason());
    }

    @Test
    void getPurchasedTickets_shouldReturnAllPurchasedTicketsConverted() {
        TicketEntity ticketEntity = TicketEntity.builder()
                .id(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(10)
                .build();
        PurchasedTicketEntity purchasedTicketEntity = PurchasedTicketEntity.builder()
                .id(1)
                .ticket(ticketEntity)
                .purchaseQuantity(5)
                .purchaseDate(LocalDateTime.now())
                .build();
        PurchasedTicketDTO purchasedTicketDTO = PurchasedTicketDTO.builder()
                .ticketId(1)
                .ticketName("VIP")
                .price(100.0)
                .quantity(5)
                .eventName("Concert")
                .location("Amsterdam")
                .build();

        when(purchasedTicketRepository.findAll()).thenReturn(List.of(purchasedTicketEntity));
        when(ticketMapper.toPurchasedTicketDTO(purchasedTicketEntity)).thenReturn(purchasedTicketDTO);

        List<PurchasedTicketDTO> result = ticketService.getPurchasedTickets();

        assertEquals(1, result.size());
        assertEquals(purchasedTicketDTO, result.get(0));
        verify(purchasedTicketRepository).findAll();
        verify(ticketMapper).toPurchasedTicketDTO(purchasedTicketEntity);
    }

}
