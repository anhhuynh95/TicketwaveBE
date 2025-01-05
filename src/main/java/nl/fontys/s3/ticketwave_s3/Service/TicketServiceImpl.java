package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.PurchasedTicketRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found.";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found.";

    private final TicketRepository ticketRepository;

    private final EventRepository eventRepository;

    private final PurchasedTicketRepository purchasedTicketRepository;

    private final EventMapper eventMapper;

    private final TicketMapper ticketMapper;

    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, EventRepository eventRepository, PurchasedTicketRepository purchasedTicketRepository, EventMapper eventMapper, TicketMapper ticketMapper, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.purchasedTicketRepository = purchasedTicketRepository;
        this.eventMapper = eventMapper;
        this.ticketMapper = ticketMapper;
        this.userRepository = userRepository;
    }

    /** Get all tickets. */
    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /** Find ticket by ID. */
    @Override
    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE));
    }

    /** Get tickets under specified max price. */
    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getPrice() <= maxPrice)
                .toList();
    }

    /** Create a new ticket for an existing event. */
    @Override
    public void createTicket(Ticket ticket) {
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    /** Update an existing ticket. */
    @Override
    public void updateTicket(Integer id, Ticket ticket) {
        Ticket existingTicket = getTicketById(id);
        if (existingTicket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }
        ticket.setId(id);
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    /** Delete a ticket by ID. */
    @Override
    public void deleteTicket(Integer id) {
        Ticket ticket = getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }
        ticketRepository.deleteById(id);
    }

    /** Purchase a specified quantity of a ticket. */
    @Override
    @Transactional
    public void purchaseTicket(Integer ticketId, Integer quantity, Integer userId) {

        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }

        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }

        if (event.getTicketQuantity() < quantity || ticket.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available.");
        }

        // Deduct from EventEntity's ticketQuantity
        event.setTicketQuantity(event.getTicketQuantity() - quantity);
        eventRepository.save(event);

        // Deduct from TicketEntity's quantity
        TicketEntity ticketEntity = ticketRepository.findEntityById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket entity not found."));
        ticketEntity.setQuantity(ticketEntity.getQuantity() - quantity);
        ticketRepository.saveEntity(ticketEntity);

        // Save the purchase
        PurchasedTicketEntity purchasedTicket = new PurchasedTicketEntity();
        purchasedTicket.setTicket(ticketEntity);
        purchasedTicket.setPurchaseQuantity(quantity);
        purchasedTicket.setPurchaseDate(LocalDateTime.now());
        purchasedTicket.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")));

        purchasedTicketRepository.saveAndFlush(purchasedTicket);
    }

    /** Get all purchased tickets. */
    @Override
    public List<PurchasedTicketDTO> getPurchasedTickets(Integer userId) {
        return purchasedTicketRepository.findByUserId(userId).stream()
                .map(ticketMapper::toPurchasedTicketDTO)
                .toList();
    }

    /** Get tickets by event ID. */
    @Override
    public List<Ticket> getTicketsByEventId(Integer eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    /** Cancel specified quantity of purchased tickets. */
    @Override
    @Transactional
    public void cancelTickets(Integer ticketId, Integer cancelQuantity) {
        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }

        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }

        List<PurchasedTicketEntity> purchasedTickets = purchasedTicketRepository.findByTicketId(ticketId);
        if (purchasedTickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchased ticket record not found.");
        }

        // Calculate the total purchased quantity for the ticket
        int totalPurchasedQuantity = purchasedTickets.stream()
                .mapToInt(PurchasedTicketEntity::getPurchaseQuantity)
                .sum();

        // Check if we have enough quantity to cancel
        if (cancelQuantity > totalPurchasedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel more tickets than purchased.");
        }

        // Iterate over purchased tickets to cancel tickets across multiple records if necessary
        int remainingCancelQuantity = cancelQuantity;
        for (PurchasedTicketEntity purchasedTicket : purchasedTickets) {
            int purchaseQuantity = purchasedTicket.getPurchaseQuantity();

            if (remainingCancelQuantity >= purchaseQuantity) {
                // Cancel the entire record
                remainingCancelQuantity -= purchaseQuantity;
                purchasedTicketRepository.delete(purchasedTicket);
            } else {
                // Partially cancel this record
                purchasedTicket.setPurchaseQuantity(purchaseQuantity - remainingCancelQuantity);
                purchasedTicketRepository.save(purchasedTicket);
                break;
            }
        }

        // Restore quantities in Event and Ticket entities
        event.setTicketQuantity(event.getTicketQuantity() + cancelQuantity);
        eventRepository.save(event);

        TicketEntity ticketEntity = ticketRepository.findEntityById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket entity not found."));
        ticketEntity.setQuantity(ticketEntity.getQuantity() + cancelQuantity);
        ticketRepository.saveEntity(ticketEntity);
    }

    public Map<String, Long> getTotalPurchasesByEventType() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> results = purchasedTicketRepository.findPurchasesByEventType(sixMonthsAgo);

        System.out.println("Results from findPurchasesByEventType: " + results);

        if (results.isEmpty()) {
            System.out.println("No data found for purchases by event type.");
            return Collections.emptyMap();
        }

        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((EventType) result[0]).name(),
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Double> getMonthlySales(LocalDateTime startDate) {
        List<Object[]> results = purchasedTicketRepository.findMonthlySales(startDate);

        System.out.println("Results from findMonthlySales: " + results);

        if (results.isEmpty()) {
            System.out.println("No data found for monthly sales.");
            return Collections.emptyMap();
        }

        return results.stream()
                .collect(Collectors.toMap(
                        row -> {
                            int year = (int) row[0];
                            int month = (int) row[1];
                            // Convert the month number to the full month name and format with year
                            String monthName = Month.of(month).name();
                            return monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase() + " " + year;
                        },
                        row -> ((Number) row[2]).doubleValue() // Convert the sales value to Double
                ));
    }

}
