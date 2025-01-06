package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.NotificationDTO;
import nl.fontys.s3.ticketwave_s3.Domain.AdminNotificationEvent;
import nl.fontys.s3.ticketwave_s3.Mapper.NotificationMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.NotificationEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.CommentDBRepository;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.NotificationRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.context.event.EventListener;
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
    private final EmailService emailService;

    @EventListener
    public void handleAdminNotification(AdminNotificationEvent event) {
        // Step 1: Broadcast the notification via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/admin-notifications",
                Map.of(
                        "message", event.getMessage(),
                        "userId", event.getUserId(),
                        "commentId", event.getCommentId(),
                        "eventId", event.getEventId()
                )
        );
        System.out.println("Admin notified: " + event.getMessage());

        // Step 2: Save the notification to the database
        LocalDateTime recentTimestamp = LocalDateTime.now().minusMinutes(5);
        if (!notificationRepository.existsByMessageUserAndRecentTimestamp(event.getMessage(), event.getUserId(), recentTimestamp)) {
            saveAdminNotification(event.getMessage(), event.getUserId(), event.getCommentId());
        } else {
            System.out.println("Duplicate notification detected within the threshold. Skipping save.");
        }
    }

    public void notifyAdmins(String message, Integer userId, Integer commentId) {
        LocalDateTime recentTimestamp = LocalDateTime.now().minusMinutes(5);

        NotificationDTO notification = NotificationDTO.builder()
                .userId(userId)
                .commentId(commentId)
                .message(message)
                .createdAt(LocalDateTime.now())
                .resolved(false)
                .build();

        System.out.println("WebSocket: Sending to /topic/admin-notifications - " + notification);

        // Broadcast the notification via WebSocket
        messagingTemplate.convertAndSend("/topic/admin-notifications", notification);

        // Save the notification to the database if it's not a duplicate
        if (!notificationRepository.existsByMessageUserAndRecentTimestamp(message, userId, recentTimestamp)) {
            saveAdminNotification(message, userId, commentId);
        } else {
            System.out.println("Duplicate notification detected within the threshold. Skipping send.");
        }
    }

    public void notifyUser(Integer userId, String message) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);
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

    public void sendWarnEmail(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        String subject = "⚠️ Warning from Ticketwave Admin";
        String text = "Dear " + user.getUsername() + ",\n\n"
                + "We noticed some inappropriate behavior on your account. "
                + "Please adhere to our community guidelines to avoid further actions.\n\n"
                + "Best regards,\nTicketwave Admin Team";

        emailService.sendEmail(user.getUsername(), subject, text);
    }

    public void sendBanEmail(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        String subject = "🚫 Account Ban from Ticketwave Admin";
        String text = "Dear " + user.getUsername() + ",\n\n"
                + "Due to repeated violations of our community guidelines, "
                + "your account has been banned. If you believe this is a mistake, "
                + "please contact support.\n\n"
                + "Best regards,\nTicketwave Admin Team";

        emailService.sendEmail(user.getUsername(), subject, text);
    }
}
