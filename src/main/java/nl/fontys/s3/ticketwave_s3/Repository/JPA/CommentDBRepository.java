package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentDBRepository extends JpaRepository<CommentEntity, Integer> {
    List<CommentEntity> findByEventId(Integer eventId);
}
