package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Mapper.CommentMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void addComment_shouldAddCommentSuccessfully() {
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(1)
                .eventId(1)
                .commentText("Great event!")
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .id(1)
                .userId(1)
                .eventId(1)
                .commentText("Great event!")
                .build();

        when(commentMapper.toEntity(commentDTO)).thenReturn(commentEntity);
        when(commentRepository.save(commentEntity)).thenReturn(commentEntity);
        when(commentMapper.toDTO(commentEntity)).thenReturn(commentDTO);
        when(userService.findUsernameById(1)).thenReturn("testuser");
        when(commentMapper.toDomain(commentEntity, "testuser")).thenReturn(commentDTO);

        CommentDTO result = commentService.addComment(commentDTO);

        assertNotNull(result);
        assertEquals(commentDTO, result);
        verify(commentRepository).save(commentEntity);
        verify(moderationService).checkAndFlagComment(commentDTO);
        verify(userService).findUsernameById(1);
    }

    @Test
    void addComment_shouldThrowExceptionWhenUserIdIsNull() {
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(null)
                .eventId(1)
                .commentText("Great event!")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.addComment(commentDTO));

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(commentRepository, never()).save(any());
        verify(moderationService, never()).checkAndFlagComment(any());
    }

    @Test
    void getCommentsByEventId_shouldReturnCommentsForEvent() {
        Integer eventId = 1;

        CommentEntity commentEntity1 = CommentEntity.builder()
                .id(1)
                .userId(1)
                .eventId(eventId)
                .commentText("Awesome event!")
                .build();

        CommentEntity commentEntity2 = CommentEntity.builder()
                .id(2)
                .userId(2)
                .eventId(eventId)
                .commentText("Loved it!")
                .build();

        CommentDTO commentDTO1 = CommentDTO.builder()
                .id(1)
                .userId(1)
                .eventId(eventId)
                .commentText("Awesome event!")
                .username("user1")
                .build();

        CommentDTO commentDTO2 = CommentDTO.builder()
                .id(2)
                .userId(2)
                .eventId(eventId)
                .commentText("Loved it!")
                .username("user2")
                .build();

        when(commentRepository.findByEventId(eventId)).thenReturn(List.of(commentEntity1, commentEntity2));
        when(userService.findUsernameById(1)).thenReturn("user1");
        when(userService.findUsernameById(2)).thenReturn("user2");
        when(commentMapper.toDomain(commentEntity1, "user1")).thenReturn(commentDTO1);
        when(commentMapper.toDomain(commentEntity2, "user2")).thenReturn(commentDTO2);

        List<CommentDTO> result = commentService.getCommentsByEventId(eventId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(commentDTO1, result.get(0));
        assertEquals(commentDTO2, result.get(1));
        verify(commentRepository).findByEventId(eventId);
        verify(userService, times(2)).findUsernameById(anyInt());
    }

    @Test
    void getCommentsByEventId_shouldReturnEmptyListWhenNoComments() {
        Integer eventId = 1;

        when(commentRepository.findByEventId(eventId)).thenReturn(List.of());

        List<CommentDTO> result = commentService.getCommentsByEventId(eventId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findByEventId(eventId);
        verify(userService, never()).findUsernameById(anyInt());
    }
}
