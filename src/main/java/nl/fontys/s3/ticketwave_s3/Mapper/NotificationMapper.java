package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.NotificationDTO;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(NotificationEntity entity) {
        return NotificationDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .commentId(entity.getComment().getId() != null ? entity.getComment().getId() : null)
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .build();
    }
}
