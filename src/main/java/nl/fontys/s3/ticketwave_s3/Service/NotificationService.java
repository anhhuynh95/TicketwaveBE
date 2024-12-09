package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.NotificationDTO;
import nl.fontys.s3.ticketwave_s3.Mapper.NotificationMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.NotificationEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.NotificationRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentDBRepository commentRepository;
    private final NotificationMapper notificationMapper;

    public void notifyAdmins(String message, Integer userId, Integer commentId) {
        LocalDateTime recentTimestamp = LocalDateTime.now().minusMinutes(5);

        Map<String, Object> notification = Map.of(
                "type", "TOXIC_COMMENT",
                "message", message,
                "userId", userId,
                "commentId", commentId,
                "timestamp", System.currentTimeMillis()
        );

        System.out.println("WebSocket: Sending to /topic/admin-notifications - " + notification);

        if (!notificationRepository.existsByMessageUserAndRecentTimestamp(message, userId, recentTimestamp)) {
            messagingTemplate.convertAndSend("/topic/admin-notifications", notification);
            saveAdminNotification(message, userId, commentId);
        } else {
            System.out.println("Duplicate notification for the same user. Skipping send.");
        }
    }

    public void notifyUser(Integer userId, String message) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);
    }

    public void notifyAdminOnCommentDeletion(Integer commentId) {
        messagingTemplate.convertAndSend("/topic/admin-notifications", "Comment with ID " + commentId + " has been deleted.");
    }

    public void saveUserNotification(Integer userId, String message) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public void saveAdminNotification(String message, Integer userId, Integer commentId) {
        UserEntity user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        CommentEntity comment = commentId != null ? commentRepository.findById(commentId).orElse(null) : null;

        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .comment(comment)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public List<NotificationDTO> getAdminNotifications() {
        return notificationRepository.findAdminNotifications()
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }
}
