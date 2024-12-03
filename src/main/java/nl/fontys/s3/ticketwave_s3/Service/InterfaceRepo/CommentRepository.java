package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    List<CommentEntity> findByEventId(Integer eventId);
    Optional<CommentEntity> findById(Integer id);
    CommentEntity save(CommentEntity commentEntity);
}
