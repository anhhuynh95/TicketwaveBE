package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDBRepository extends JpaRepository<UserEntity, Integer> {
}
