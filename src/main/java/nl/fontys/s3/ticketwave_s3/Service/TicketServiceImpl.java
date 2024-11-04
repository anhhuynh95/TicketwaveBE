package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.TicketService;
import nl.fontys.s3.ticketwave_s3.Domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    private final List<Ticket> purchasedTickets = new ArrayList<>();
    private final Map<Integer, Integer> purchasedTicketQuantities = new HashMap<>();

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Integer id) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        return ticket;
    }

    @Override
    public List<Ticket> getTicketsByPrice(Double maxPrice) {
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getPrice() <= maxPrice) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
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
        Ticket existingTicket = ticketRepository.findById(id);
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
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }
        ticketRepository.deleteById(id);
    }

    @Override
    public void purchaseTicket(Integer id, Integer quantity) {
        Ticket ticket = getTicketById(id);
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        // Check if enough tickets are available
        if (event.getTicketQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available.");
        }

        // Deduct the purchased quantity from the event's available ticket count
        event.setTicketQuantity(event.getTicketQuantity() - quantity);
        eventRepository.save(event);

        // Update the purchased quantity for the ticket
       // ticket.setPurchasedQuantity((ticket.getPurchasedQuantity() == null ? 0 : ticket.getPurchasedQuantity()) + quantity);
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);

        // Track purchased tickets in the list if it’s not already added
        if (!purchasedTickets.contains(ticket)) {
            purchasedTickets.add(ticket);
        }
    }

    @Override
    public List<Ticket> getPurchasedTickets() {
        return new ArrayList<>(purchasedTickets);
    }

    @Override
    public List<Ticket> getTicketsByEventId(Integer eventId) {
        return ticketRepository.findByEventId(eventId);
    }
    @Override
    public void cancelTickets(Integer ticketId, Integer cancelQuantity) {
        // Retrieve the ticket by ID
        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found.");
        }

        // Retrieve the associated event
        Event event = eventRepository.findById(ticket.getEventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }

        // Ensure cancel quantity is not greater than the purchased quantity
        if (cancelQuantity > ticket.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel more tickets than purchased.");
        }

        // Update ticket quantity and event available tickets
        ticket.setQuantity(ticket.getQuantity() - cancelQuantity);
        event.setTicketQuantity(event.getTicketQuantity() + cancelQuantity);

        // Save the updated event and ticket
        eventRepository.save(event);
        EventEntity eventEntity = eventMapper.toEntity(event);
        ticketRepository.save(ticket, eventEntity);
    }

}
