package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Domain.AdminNotificationEvent;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.NotificationRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ModerationService moderationService;

    @Test
    void checkAndFlagComment_shouldFlagToxicCommentAndPublishEvent() {
        // Arrange
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(100)
                .eventId(200)
                .username("testUser")
                .commentText("This is a toxic comment!")
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .id(1)
                .userId(100)
                .eventId(200)
                .toxic(false)
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(commentEntity));
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(
                        "attributeScores", Map.of(
                                "TOXICITY", Map.of(
                                        "summaryScore", Map.of("value", 0.9)
                                )
                        )
                ), HttpStatus.OK));

        // Act
        moderationService.checkAndFlagComment(commentDTO);

        // Assert
        assertTrue(commentEntity.isToxic());
        verify(commentRepository).save(commentEntity);
        verify(messagingTemplate).convertAndSend("/topic/comment-updates/200", Map.of("flaggedCommentId", 1, "isToxic", true));
        verify(eventPublisher).publishEvent(any(AdminNotificationEvent.class));
    }

    @Test
    void checkAndFlagComment_shouldNotPublishEventForNonToxicComment() {
        // Arrange
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1)
                .userId(100)
                .eventId(200)
                .username("testUser")
                .commentText("This is a polite comment!")
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(
                        "attributeScores", Map.of(
                                "TOXICITY", Map.of(
                                        "summaryScore", Map.of("value", 0.1)
                                )
                        )
                ), HttpStatus.OK));

        // Act
        moderationService.checkAndFlagComment(commentDTO);

        // Assert
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(messagingTemplate);
    }
}


