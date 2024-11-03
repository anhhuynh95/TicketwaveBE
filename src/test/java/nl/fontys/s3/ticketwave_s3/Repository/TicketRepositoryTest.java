package nl.fontys.s3.ticketwave_s3.Repository;

import jakarta.persistence.EntityManager;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.TicketEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.TicketDBRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TicketDBRepository ticketDBRepository;

    @Test
    void save_shouldSaveTicketWithAllFields() {
        // Arrange
        EventEntity eventEntity = new EventEntity(null, "Concert A", "Eindhoven", "Exciting concert", "2024-11-01T20:00", 100, null);
        entityManager.persist(eventEntity);

        // Act
        TicketEntity ticketEntity = new TicketEntity(null, eventEntity, "VIP", 50.0, 10); // Correct constructor order
        TicketEntity savedTicket = ticketDBRepository.save(ticketEntity);

        // Assert
        assertNotNull(savedTicket.getId());
        assertEquals("VIP", savedTicket.getTicketName());
        assertEquals(50.0, savedTicket.getPrice());
        assertEquals(eventEntity.getId(), savedTicket.getEvent().getId());
    }

    @Test
    void findById_shouldReturnTicket_whenTicketExists() {
        // Arrange
        EventEntity eventEntity = new EventEntity(null, "Concert B", "Amsterdam", "Great concert", "2024-12-01T18:00", 50, null);
        entityManager.persist(eventEntity);

        TicketEntity ticketEntity = new TicketEntity(null, eventEntity, "Regular", 75.0, 5); // Correct constructor order
        TicketEntity savedTicket = ticketDBRepository.save(ticketEntity);

        // Act
        Optional<TicketEntity> foundTicket = ticketDBRepository.findById(savedTicket.getId());

        // Assert
        assertTrue(foundTicket.isPresent());
        assertEquals("Regular", foundTicket.get().getTicketName());
        assertEquals(75.0, foundTicket.get().getPrice());
        assertEquals(eventEntity.getId(), foundTicket.get().getEvent().getId());
    }

}