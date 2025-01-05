package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.NotificationRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ModerationService moderationService;

    @Test
    void checkAndFlagComment_shouldFlagToxicComment() {
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
                                        "summaryScore", Map.of("value", 0.8)
                                )
                        )
                ), HttpStatus.OK));

        when(notificationRepository.existsByMessageUserAndRecentTimestamp(anyString(), anyInt(), any()))
                .thenReturn(false);

        moderationService.checkAndFlagComment(commentDTO);

        assertTrue(commentEntity.isToxic());
        verify(commentRepository).save(commentEntity);
        verify(messagingTemplate).convertAndSend("/topic/comment-updates/200", Map.of("flaggedCommentId", 1, "isToxic", true));
        verify(notificationService).notifyAdmins(anyString(), eq(100), eq(1));
    }

    @Test
    void checkAndFlagComment_shouldNotFlagNonToxicComment() {
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

        moderationService.checkAndFlagComment(commentDTO);

        verifyNoInteractions(commentRepository, notificationService, messagingTemplate);
    }

    @Test
    void checkAndFlagComment_shouldSkipDuplicateNotification() {
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

        when(notificationRepository.existsByMessageUserAndRecentTimestamp(anyString(), anyInt(), any()))
                .thenReturn(true);

        moderationService.checkAndFlagComment(commentDTO);

        verify(commentRepository).save(commentEntity);
        verify(messagingTemplate).convertAndSend("/topic/comment-updates/200", Map.of("flaggedCommentId", 1, "isToxic", true));
        verifyNoInteractions(notificationService);
    }
}
