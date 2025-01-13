package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.Event;
import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventRepository;
import nl.fontys.s3.ticketwave_s3.Mapper.EventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void getAllEvents_shouldReturnPaginatedEvents() {
        List<Event> events = List.of(
                Event.builder().id(1).name("Concert").location("Amsterdam").build(),
                Event.builder().id(2).name("Festival").location("Rotterdam").build()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(events, pageable, events.size());
        when(eventRepository.findAll(pageable)).thenReturn(eventPage);

        Page<Event> result = eventService.getAllEvents(pageable);

        assertEquals(events, result.getContent());
        assertEquals(2, result.getTotalElements());
        verify(eventRepository).findAll(pageable);
    }

    @Test
    void getAllEvents_shouldReturnEmptyPageWhenNoEventsFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(eventRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Event> result = eventService.getAllEvents(pageable);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
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

        double[] mockCoordinates = {52.3676, 4.9041};
        when(geocodingService.getCoordinates("Amsterdam")).thenReturn(mockCoordinates);

        eventService.createEvent(event);

        assertEquals(mockCoordinates[0], event.getLatitude());
        assertEquals(mockCoordinates[1], event.getLongitude());
        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_shouldUpdateExistingEvent() {
        Event event = Event.builder()
                .id(1)
                .name("Updated Concert")
                .location("Amsterdam")
                .build();

        double[] mockCoordinates = {52.3676, 4.9041};
        when(geocodingService.getCoordinates("Amsterdam")).thenReturn(mockCoordinates);

        eventService.updateEvent(1, event);

        assertEquals(mockCoordinates[0], event.getLatitude());
        assertEquals(mockCoordinates[1], event.getLongitude());
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

    @Test
    void searchEvents_shouldReturnPaginatedResultsWhenQueryExists() {
        String query = "concert";
        EventType eventType = EventType.MUSIC;
        Pageable pageable = PageRequest.of(0, 5);

        EventEntity eventEntity = EventEntity.builder().id(1).name("Concert").location("Amsterdam").build();
        Event event = Event.builder().id(1).name("Concert").location("Amsterdam").build();

        List<EventEntity> eventEntities = List.of(eventEntity);
        List<Event> events = List.of(event);

        Page<EventEntity> eventEntityPage = new PageImpl<>(eventEntities, pageable, eventEntities.size());

        when(eventRepository.searchEvents(query, eventType, null, null, null, pageable)).thenReturn(eventEntityPage);
        when(eventMapper.toDomain(eventEntity)).thenReturn(event);

        Page<Event> result = eventService.searchEvents(query, eventType, null, null, null, pageable);

        assertEquals(events, result.getContent());
        verify(eventRepository).searchEvents(query, eventType, null, null, null, pageable);
    }

    @Test
    void searchEvents_shouldReturnEmptyPageWhenNoResultsFound() {
        String query = "nonexistent";
        Pageable pageable = PageRequest.of(0, 5);

        Page<EventEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(eventRepository.searchEvents(query, null, null, null, null, pageable)).thenReturn(emptyPage);

        Page<Event> result = eventService.searchEvents(query, null, null, null, null, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(eventRepository).searchEvents(query, null, null, null, null, pageable);
    }
}
