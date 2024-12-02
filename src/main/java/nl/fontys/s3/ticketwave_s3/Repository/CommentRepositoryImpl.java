package nl.fontys.s3.ticketwave_s3.Repository;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentDBRepository commentDBRepository;

    @Override
    public List<CommentEntity> findByEventId(Integer eventId) {
        return commentDBRepository.findByEventId(eventId);
    }
}
