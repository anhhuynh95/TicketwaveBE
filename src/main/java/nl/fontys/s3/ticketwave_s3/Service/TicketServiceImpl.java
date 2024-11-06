package nl.fontys.s3.ticketwave_s3.Service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PurchasedTicketRepository purchasedTicketRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));
    }

    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    @Override
    public void createTicket(Ticket ticket) {
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    @Override
    public void updateTicket(Integer id, Ticket ticket) {
        Ticket existingTicket = getTicketById(id);
        if (existingTicket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        ticket.setId(id);  // Ensure the ticket ID is set correctly
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

    @Override
    public void deleteTicket(Integer id) {
        Ticket ticket = getTicketById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void purchaseTicket(Integer ticketId, Integer quantity) {

        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
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
    public List<Ticket> getPurchasedTickets() {
        return purchasedTicketRepository.findAll().stream()
                .map(this::mapToPurchasedTicketDomain)
                .collect(Collectors.toList());
    }

    private Ticket mapToPurchasedTicketDomain(PurchasedTicketEntity purchasedTicketEntity) {
        Ticket ticket = ticketMapper.toDomain(purchasedTicketEntity.getTicket());
        ticket.setQuantity(purchasedTicketEntity.getPurchaseQuantity());
        return ticket;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        List<PurchasedTicketEntity> purchasedTickets = purchasedTicketRepository.findByTicketId(ticketId);
        if (purchasedTickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchased ticket record not found.");
        }

        PurchasedTicketEntity purchasedTicket = purchasedTickets.get(0);

        if (cancelQuantity > purchasedTicket.getPurchaseQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel more tickets than purchased.");
        }

        if (cancelQuantity.equals(purchasedTicket.getPurchaseQuantity())) {
            purchasedTicketRepository.delete(purchasedTicket);
        } else {
            purchasedTicket.setPurchaseQuantity(purchasedTicket.getPurchaseQuantity() - cancelQuantity);
            purchasedTicketRepository.save(purchasedTicket);
        }

        // Restore the quantity in EventEntity
        event.setTicketQuantity(event.getTicketQuantity() + cancelQuantity);
        eventRepository.save(event);

        // Restore the quantity in TicketEntity
        TicketEntity ticketEntity = ticketRepository.findEntityById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket entity not found."));
        ticketEntity.setQuantity(ticketEntity.getQuantity() + cancelQuantity);
        ticketRepository.saveEntity(ticketEntity);

    }

}
