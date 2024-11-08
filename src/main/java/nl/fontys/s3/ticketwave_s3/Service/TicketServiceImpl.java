package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.PurchasedTicketDTO;
import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Mapper.TicketMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.PurchasedTicketEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.PurchasedTicketRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found.";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found.";

    private final TicketRepository ticketRepository;

    private final EventRepository eventRepository;

    private final PurchasedTicketRepository purchasedTicketRepository;

    private final EventMapper eventMapper;

    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository, EventRepository eventRepository, PurchasedTicketRepository purchasedTicketRepository, EventMapper eventMapper, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.purchasedTicketRepository = purchasedTicketRepository;
        this.eventMapper = eventMapper;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE));
    }

    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getPrice() <= maxPrice)
                .toList();
    }

    @Override
    public void createTicket(Ticket ticket) {
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    @Override
    public void updateTicket(Integer id, Ticket ticket) {
        Ticket existingTicket = getTicketById(id);
        if (existingTicket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }
        ticket.setId(id);  // Ensure the ticket ID is set correctly
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE);
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    @Override
    public void deleteTicket(Integer id) {
        Ticket ticket = getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TICKET_NOT_FOUND_MESSAGE);
        }
        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void purchaseTicket(Integer ticketId, Integer quantity) {

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

        // Save a PurchasedTicketEntity record
        PurchasedTicketEntity purchasedTicket = new PurchasedTicketEntity();
        purchasedTicket.setTicket(ticketEntity);
        purchasedTicket.setPurchaseQuantity(quantity);
        purchasedTicket.setPurchaseDate(LocalDateTime.now());

        purchasedTicketRepository.saveAndFlush(purchasedTicket);
    }


    @Override
    public List<PurchasedTicketDTO> getPurchasedTickets() {
        return purchasedTicketRepository.findAll().stream()
                .map(ticketMapper::toPurchasedTicketDTO)
                .toList();
    }

    @Override
    public List<Ticket> getTicketsByEventId(Integer eventId) {
        return ticketRepository.findByEventId(eventId);
    }

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

}
