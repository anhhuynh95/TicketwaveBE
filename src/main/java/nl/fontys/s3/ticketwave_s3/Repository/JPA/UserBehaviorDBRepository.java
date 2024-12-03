package nl.fontys.s3.ticketwave_s3.Repository.JPA;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBehaviorDBRepository extends JpaRepository<UserBehaviorEntity, Integer> {
    Optional<UserBehaviorEntity> findByUserId(Integer userId);}

