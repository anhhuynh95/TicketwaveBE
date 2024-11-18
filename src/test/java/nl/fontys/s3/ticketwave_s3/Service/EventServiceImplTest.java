package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void getAllEvents_shouldReturnPaginatedEvents() {
        List<Event> events = List.of(
                Event.builder().id(1).name("Concert").location("Amsterdam").build(),
                Event.builder().id(2).name("Festival").location("Rotterdam").build()
        );

        Pageable pageable = PageRequest.of(0, 10); // Page 0 with 10 items per page
        Page<Event> eventPage = new PageImpl<>(events, pageable, events.size());
        when(eventRepository.findAll(pageable)).thenReturn(eventPage);

        Page<Event> result = eventService.getAllEvents(pageable);

        assertEquals(events, result.getContent());
        assertEquals(2, result.getTotalElements());
        verify(eventRepository).findAll(pageable);
    }

    @Test
    void getEventById_shouldReturnEvent_whenEventExists() {
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .build();
        when(eventRepository.findById(1)).thenReturn(event);

        Event result = eventService.getEventById(1);

        assertEquals(event, result);
        verify(eventRepository).findById(1);
    }

    @Test
    void getEventById_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findById(1)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> eventService.getEventById(1));
        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository).findById(1);
    }

    @Test
    void createEvent_shouldSaveEvent() {
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .build();

        eventService.createEvent(event);

        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_shouldUpdateExistingEvent() {
        Event event = Event.builder()
                .id(1)
                .name("Updated Concert")
                .location("Amsterdam")
                .build();

        eventService.updateEvent(1, event);

        verify(eventRepository).save(event);
    }

    @Test
    void deleteEvent_shouldDeleteEvent_whenEventExists() {
        Event event = Event.builder()
                .id(1)
                .name("Concert")
                .location("Amsterdam")
                .build();
        when(eventRepository.findById(1)).thenReturn(event);

        eventService.deleteEvent(1);

        verify(eventRepository).findById(1);
        verify(eventRepository).deleteById(1);
    }

    @Test
    void deleteEvent_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findById(1)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> eventService.deleteEvent(1));
        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository).findById(1);
        verify(eventRepository, never()).deleteById(anyInt());
    }
}