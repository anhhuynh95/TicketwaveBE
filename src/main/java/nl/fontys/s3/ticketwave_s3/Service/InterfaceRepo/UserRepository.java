package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;

import java.util.Optional;

public interface UserRepository {
    Optional<UserEntity> findById(Integer id);
    void save(UserEntity userEntity);
}
