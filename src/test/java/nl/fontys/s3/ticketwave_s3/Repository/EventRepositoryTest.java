package nl.fontys.s3.ticketwave_s3.Repository;

import jakarta.persistence.EntityManager;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.EventDBRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EventDBRepository eventDBRepository;

    @Test
    void save_shouldSaveEventWithAllFields() {
        EventEntity eventEntity = new EventEntity(null, "Concert A", "Eindhoven", "Exciting concert", "2024-11-01T20:00", 100, null);

        // Act
        EventEntity savedEvent = eventDBRepository.save(eventEntity);

        // Assert
        assertNotNull(savedEvent.getId());
        assertEquals("Concert A", savedEvent.getName());
    }

    @Test
    void findById_shouldReturnEvent_whenEventExists() {
        // Arrange
        EventEntity eventEntity = new EventEntity(null, "Concert B", "Amsterdam", "Great concert", "2024-12-01T18:00", 50, null);
        EventEntity savedEvent = eventDBRepository.save(eventEntity);

        // Act
        Optional<EventEntity> foundEvent = eventDBRepository.findById(savedEvent.getId());

        // Assert
        assertTrue(foundEvent.isPresent());
        assertEquals("Concert B", foundEvent.get().getName());
    }
}