package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.CommentEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDTO toDomain(CommentEntity commentEntity, String username) {
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .eventId(commentEntity.getEventId())
                .userId(commentEntity.getUserId())
                .username(username)
                .commentText(commentEntity.getCommentText())
                .createdAt(commentEntity.getCreatedAt())
                .build();
    }

    public CommentEntity toEntity(CommentDTO commentDTO) {
        return CommentEntity.builder()
                .eventId(commentDTO.getEventId())
                .userId(commentDTO.getUserId())
                .commentText(commentDTO.getCommentText())
                .build();
    }

    public CommentDTO toDTO(CommentEntity commentEntity) {
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .eventId(commentEntity.getEventId())
                .userId(commentEntity.getUserId())
                .commentText(commentEntity.getCommentText())
                .createdAt(commentEntity.getCreatedAt())
                .build();
    }
}
