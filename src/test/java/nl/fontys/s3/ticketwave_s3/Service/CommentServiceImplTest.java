package nl.fontys.s3.ticketwave_s3.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Mapper.CommentMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentDBRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserService userService;

    @Mock
    private ModerationService moderationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void addComment_shouldAddCommentSuccessfully() throws JsonProcessingException {
        CommentDTO commentDTO = CommentDTO.builder()
                .userId(1)
                .eventId(100)
                .commentText("Test comment")
                .build();
        CommentEntity commentEntity = CommentEntity.builder().build();
        CommentEntity savedEntity = CommentEntity.builder().build();

        when(commentMapper.toEntity(commentDTO)).thenReturn(commentEntity);
        when(commentRepository.save(commentEntity)).thenReturn(savedEntity);
        when(userService.findUsernameById(1)).thenReturn("testUser");
        when(commentMapper.toDomain(savedEntity, "testUser")).thenReturn(commentDTO);
        when(objectMapper.writeValueAsString(commentDTO)).thenReturn("{\"comment\":\"serialized\"}");

        CommentDTO result = commentService.addComment(commentDTO);

        assertNotNull(result);
        assertEquals("Test comment", result.getCommentText());
        verify(moderationService).checkAndFlagComment(commentDTO);
        verify(messagingTemplate).convertAndSend("/topic/comment-updates/100", commentDTO);
    }

    @Test
    void addComment_shouldThrowExceptionWhenUserIdIsNull() {
        CommentDTO commentDTO = CommentDTO.builder()
                .userId(null)
                .eventId(100)
                .commentText("Test comment")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.addComment(commentDTO));
        assertEquals("User ID cannot be null", exception.getMessage());
        verifyNoInteractions(commentRepository, messagingTemplate, moderationService);
    }

    @Test
    void getCommentsByEventId_shouldReturnComments() {
        Integer eventId = 100;
        CommentEntity commentEntity = CommentEntity.builder().userId(1).build();
        CommentDTO commentDTO = CommentDTO.builder().userId(1).commentText("Test comment").build();

        when(commentRepository.findByEventId(eventId)).thenReturn(List.of(commentEntity));
        when(userService.findUsernameById(1)).thenReturn("testUser");
        when(commentMapper.toDomain(commentEntity, "testUser")).thenReturn(commentDTO);

        List<CommentDTO> result = commentService.getCommentsByEventId(eventId);

        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getCommentText());
        verify(commentRepository).findByEventId(eventId);
    }

    @Test
    void getCommentsByEventId_shouldReturnEmptyListWhenNoComments() {
        Integer eventId = 100;
        when(commentRepository.findByEventId(eventId)).thenReturn(List.of());

        List<CommentDTO> result = commentService.getCommentsByEventId(eventId);

        assertTrue(result.isEmpty());
        verify(commentRepository).findByEventId(eventId);
        verifyNoInteractions(userService, commentMapper);
    }

    @Test
    void deleteComment_shouldDeleteCommentSuccessfully() {
        Integer commentId = 1;
        CommentEntity commentEntity = CommentEntity.builder().id(commentId).eventId(100).build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentEntity));

        commentService.deleteComment(commentId);

        verify(commentRepository).delete(commentEntity);
        verify(messagingTemplate).convertAndSend("/topic/comment-updates/100", Map.of("deletedCommentId", commentId));
    }

    @Test
    void deleteComment_shouldThrowExceptionWhenCommentNotFound() {
        Integer commentId = 1;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(commentId));
        assertEquals("Comment not found.", exception.getMessage());
        verifyNoInteractions(messagingTemplate, notificationService);
    }
}
