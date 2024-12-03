package nl.fontys.s3.ticketwave_s3.Controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserBehaviorService;
import nl.fontys.s3.ticketwave_s3.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user-behavior")
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;
    private final NotificationService notificationService;

    @PostMapping("/warn/{userId}")
    public ResponseEntity<String> warnUser(@PathVariable Integer userId) {
        userBehaviorService.warnUser(userId);
        notificationService.notifyUser(userId, "You have been warned by an admin. Please adhere to community guidelines.");
        return ResponseEntity.ok("User warned successfully.");
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable Integer userId) {
        userBehaviorService.banUser(userId);
        return ResponseEntity.ok("User banned successfully.");
    }
}
