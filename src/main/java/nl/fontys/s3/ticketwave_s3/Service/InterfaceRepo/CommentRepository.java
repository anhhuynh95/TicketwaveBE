package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;

import java.util.List;

public interface CommentRepository {
    List<CommentEntity> findByEventId(Integer eventId);
}
