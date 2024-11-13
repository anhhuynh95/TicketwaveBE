package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDBRepository extends JpaRepository<EventEntity, Integer> {
}
