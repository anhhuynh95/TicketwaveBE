package nl.fontys.s3.ticketwave_s3.Mapper;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorMapper {
    public UserBehaviorDTO toDTO(UserBehaviorEntity userBehavior) {
        return UserBehaviorDTO.builder()
                .userId(userBehavior.getUser().getId())
                .warnings(userBehavior.getWarnings())
                .isBanned(userBehavior.isBanned())
                .build();
    }

    public UserBehaviorEntity toEntity(UserBehaviorDTO dto, UserEntity user) {
        return UserBehaviorEntity.builder()
                .user(user)
                .warnings(dto.getWarnings())
                .isBanned(dto.isBanned())
                .build();
    }
}

