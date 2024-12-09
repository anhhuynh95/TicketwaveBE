package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Integer id;
    private Integer userId;
    private Integer commentId;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
}
