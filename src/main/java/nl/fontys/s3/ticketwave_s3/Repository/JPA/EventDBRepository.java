package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Domain.EventType;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface EventDBRepository extends JpaRepository<EventEntity, Integer> {

    @Query("SELECT e FROM EventEntity e " +
            "LEFT JOIN TicketEntity t ON e.id = t.event.id " +
            "WHERE (:query IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(t.ticketName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:eventType IS NULL OR e.eventType = :eventType) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR " +
            "      (SQRT(POWER(e.latitude - :latitude, 2) + POWER(e.longitude - :longitude, 2)) * 111) <= :radius) " +
            "GROUP BY e.id " +
            "ORDER BY e.dateTime ASC")
    Page<EventEntity> searchEvents(
            @Param("query") String query,
            @Param("eventType") EventType eventType,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radius,
            Pageable pageable);
}

