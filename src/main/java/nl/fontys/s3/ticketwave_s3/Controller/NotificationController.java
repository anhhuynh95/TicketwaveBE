package nl.fontys.s3.ticketwave_s3.Controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.NotificationDTO;
import nl.fontys.s3.ticketwave_s3.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@RequestParam Integer userId) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTO>> getAdminNotifications() {
        List<NotificationDTO> notifications = notificationService.getAdminNotifications();
        return ResponseEntity.ok(notifications);
    }
}
