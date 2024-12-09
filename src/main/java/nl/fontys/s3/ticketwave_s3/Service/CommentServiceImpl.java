package nl.fontys.s3.ticketwave_s3.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.CommentService;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Mapper.CommentMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDBRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final ModerationService moderationService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CommentDTO addComment(CommentDTO commentDTO) {
        Integer userId = commentDTO.getUserId();

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        CommentEntity commentEntity = commentMapper.toEntity(commentDTO);

        CommentEntity savedEntity = commentRepository.save(commentEntity);

        String username = userService.findUsernameById(userId);
        CommentDTO createdComment = commentMapper.toDomain(savedEntity, username);

        moderationService.checkAndFlagComment(createdComment);

        // Serialize the payload for validation
        try {
            String serializedPayload = objectMapper.writeValueAsString(createdComment);
            System.out.println("WebSocket serialized payload: " + serializedPayload);
        } catch (Exception e) {
            System.err.println("Error serializing payload: " + e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/comment-updates/" + createdComment.getEventId(), createdComment);
        System.out.println("Sent WebSocket Message: " + createdComment);

        return createdComment;
    }

    @Override
    public List<CommentDTO> getCommentsByEventId(Integer eventId) {
        return commentRepository.findByEventId(eventId).stream()
                .map(commentEntity -> {
                    String username = userService.findUsernameById(commentEntity.getUserId());
                    return commentMapper.toDomain(commentEntity, username);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Integer commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        commentRepository.delete(comment);

        // Send update to the specific event topic
        messagingTemplate.convertAndSend(
                "/topic/comment-updates/" + comment.getEventId(),
                Map.of("deletedCommentId", commentId)
        );

        // Notify admins
        notificationService.notifyAdminOnCommentDeletion(commentId);
    }

}
