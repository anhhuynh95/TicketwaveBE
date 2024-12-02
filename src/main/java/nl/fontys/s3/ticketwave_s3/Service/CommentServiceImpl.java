package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.CommentService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Mapper.CommentMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDBRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    @Override
    public CommentDTO addComment(CommentDTO commentDTO) {
        Integer userId = commentDTO.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Map DTO to Entity and save to repository
        CommentEntity commentEntity = commentMapper.toEntity(commentDTO);
        CommentEntity savedEntity = commentRepository.save(commentEntity);

        // Fetch the username from UserService using userId
        String username = userService.findUsernameById(userId);

        // Map Entity to DTO and include the username
        return commentMapper.toDomain(savedEntity, username);
    }

    @Override
    public List<CommentDTO> getCommentsByEventId(Integer eventId) {
        return commentRepository.findByEventId(eventId).stream()
                .map(commentEntity -> {
                    String username = userService.findUsernameById(commentEntity.getUserId());
                    return commentMapper.toDomain(commentEntity, username); // Pass username to the mapper
                })
                .collect(Collectors.toList());
    }


}
