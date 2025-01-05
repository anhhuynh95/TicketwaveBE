package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserDBRepository userDBRepository;

    public UserRepositoryImpl(UserDBRepository userDBRepository) {
        this.userDBRepository = userDBRepository;
    }

    @Override
    public void save(UserEntity userEntity) {
        userDBRepository.save(userEntity);
    }

    @Override
    public Optional<UserEntity> findById(Integer id) {
        return userDBRepository.findById(id);
    }

}
