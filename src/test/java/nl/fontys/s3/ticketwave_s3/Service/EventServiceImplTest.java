package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleEvent = new Event(1, "Concert A", "Eindhoven", "An exciting concert event", "2024-09-01T20:00", 100);
    }

    @Test
    void getAllEvents_shouldReturnAllEvents() {
        List<Event> events = List.of(sampleEvent, new Event(2, "Art Exhibition", "Nuenen", "A stunning art exhibition", "2024-09-05T18:00", 50));
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getEventById_shouldReturnEvent_whenEventExists() {
        when(eventRepository.findById(sampleEvent.getId())).thenReturn(sampleEvent);

        Event event = eventService.getEventById(sampleEvent.getId());

        assertNotNull(event);
        assertEquals("Concert A", event.getName());
        verify(eventRepository, times(1)).findById(sampleEvent.getId());
    }

    @Test
    void getEventById_shouldThrowException_whenEventDoesNotExist() {
        when(eventRepository.findById(anyInt())).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> eventService.getEventById(999));
        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository, times(1)).findById(999);
    }

    @Test
    void createEvent_shouldAddNewEvent() {
        Event newEvent = new Event(null, "Dance Concert", "Rotterdam", "Exciting dance concert", "2024-09-01T20:00", 75);

        eventService.createEvent(newEvent);

        verify(eventRepository, times(1)).save(newEvent);
    }

    @Test
    void updateEvent_shouldModifyExistingEvent() {
        Event updatedEvent = new Event(1, "Updated Event", "Updated Location", "Updated Description", "2024-09-15T20:00", 50);

        eventService.updateEvent(1, updatedEvent);

        verify(eventRepository, times(1)).save(updatedEvent);
    }



    @Test
    void deleteEvent_shouldRemoveExistingEvent() {
        when(eventRepository.findById(sampleEvent.getId())).thenReturn(sampleEvent);

        eventService.deleteEvent(sampleEvent.getId());

        verify(eventRepository, times(1)).deleteById(sampleEvent.getId());
    }

    @Test
    void deleteEvent_shouldThrowException_whenEventDoesNotExist() {
        when(eventRepository.findById(anyInt())).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> eventService.deleteEvent(999));
        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository, times(1)).findById(999);
    }
}
