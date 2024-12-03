package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserBehaviorService;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserBehaviorService userBehaviorService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ModerationService moderationService;

    @Test
    void checkAndFlagComment_shouldFlagAndNotifyWhenCommentIsToxic() {
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(1)
                .username("testuser")
                .commentText("This is a toxic comment!")
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .id(1)
                .userId(1)
                .toxic(false)
                .build();

        UserBehaviorDTO userBehaviorDTO = UserBehaviorDTO.builder()
                .userId(1)
                .warnings(1)
                .build();

        // Mock external API response for toxicity
        Map<String, Object> mockResponseBody = Map.of(
                "attributeScores", Map.of(
                        "TOXICITY", Map.of(
                                "summaryScore", Map.of("value", 0.8) // High toxicity score
                        )
                )
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));

        when(commentRepository.findById(1)).thenReturn(Optional.of(commentEntity));
        when(userBehaviorService.getUserBehavior(1)).thenReturn(userBehaviorDTO);
        doNothing().when(userBehaviorService).warnUser(1);
        doNothing().when(notificationService).notifyAdmins(anyString());

        moderationService.checkAndFlagComment(commentDTO);

        assertTrue(commentEntity.isToxic());
        verify(commentRepository).save(commentEntity);
        verify(userBehaviorService).warnUser(1);
        verify(notificationService).notifyAdmins(anyString());
    }

    @Test
    void checkAndFlagComment_shouldThrowExceptionWhenApiFails() {
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(1)
                .commentText("This is a comment.")
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("API failure"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> moderationService.checkAndFlagComment(commentDTO));
        assertEquals("Error during toxicity check", exception.getMessage());
    }
}
