package nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo;

import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    void save(UserEntity userEntity);
}
