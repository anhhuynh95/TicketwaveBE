package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.NotificationDTO;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(NotificationEntity notificationEntity) {
        return NotificationDTO.builder()
                .id(notificationEntity.getId())
                .message(notificationEntity.getMessage())
                .commentId(notificationEntity.getComment() != null ? notificationEntity.getComment().getId() : null)
                .createdAt(notificationEntity.getCreatedAt())
                .userId(notificationEntity.getUser() != null ? notificationEntity.getUser().getId() : null)
                .build();
    }
}
