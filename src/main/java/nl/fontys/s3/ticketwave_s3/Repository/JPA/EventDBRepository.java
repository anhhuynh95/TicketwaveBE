package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface EventDBRepository extends JpaRepository<EventEntity, Integer> {
   @Query("SELECT e FROM EventEntity e " +
           "LEFT JOIN TicketEntity t ON e.id = t.event.id " +
           "WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(t.ticketName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY e.id " +
           "ORDER BY e.dateTime ASC")
   Page<EventEntity> searchEvents(@Param("search") String search, Pageable pageable);
 }
