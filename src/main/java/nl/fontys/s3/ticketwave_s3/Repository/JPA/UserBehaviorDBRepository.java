package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBehaviorDBRepository extends JpaRepository<UserBehaviorEntity, Integer> {
    Optional<UserBehaviorEntity> findByUserId(Integer userId);}

