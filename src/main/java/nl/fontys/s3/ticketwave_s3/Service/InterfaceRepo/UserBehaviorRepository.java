package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;

import java.util.Optional;

public interface UserBehaviorRepository {
    Optional<UserBehaviorEntity> findByUserId(Integer userId);

    UserBehaviorEntity save(UserBehaviorEntity userBehaviorEntity);

}
