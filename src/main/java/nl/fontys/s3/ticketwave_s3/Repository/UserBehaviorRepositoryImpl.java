package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserBehaviorDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserBehaviorRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserBehaviorRepositoryImpl implements UserBehaviorRepository {

    private final UserBehaviorDBRepository userBehaviorDBRepository;

    public UserBehaviorRepositoryImpl(UserBehaviorDBRepository userBehaviorDBRepository) {
        this.userBehaviorDBRepository = userBehaviorDBRepository;
    }

    @Override
    public Optional<UserBehaviorEntity> findByUserId(Integer userId) {
        return userBehaviorDBRepository.findByUserId(userId);
    }

    @Override
    public UserBehaviorEntity save(UserBehaviorEntity userBehaviorEntity) {
        return userBehaviorDBRepository.save(userBehaviorEntity);
    }

}
