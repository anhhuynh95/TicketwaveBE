package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserDBRepository userDBRepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserDBRepository userDBRepository, UserMapper userMapper) {
        this.userDBRepository = userDBRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Optional<UserEntity> userEntity = userDBRepository.findByUsername(username);
        System.out.println("Fetched UserEntity: " + userEntity);
        return userEntity.map(userMapper::toDomain);
    }

    @Override
    public void save(UserEntity userEntity) {
        userDBRepository.save(userEntity);
    }
}
