package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserBehaviorService;
import nl.fontys.s3.ticketwave_s3.Mapper.UserBehaviorMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserBehaviorRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImp implements UserBehaviorService {

    private final UserBehaviorRepository userBehaviorRepository;
    private final UserRepository userRepository;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public UserBehaviorDTO getUserBehavior(Integer userId) {
        return userBehaviorRepository.findByUserId(userId)
                .map(userBehaviorMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User behavior not found."));
    }

    public void warnUser(Integer userId) {
        UserBehaviorEntity userBehavior = userBehaviorRepository.findByUserId(userId)
                .orElseGet(() -> createUserBehavior(userId));

        if (userBehavior.isBanned()) {
            throw new IllegalStateException("User is already banned.");
        }

        userBehavior.setWarnings(userBehavior.getWarnings() + 1);
        userBehaviorRepository.save(userBehavior);
    }

    public void banUser(Integer userId) {
        UserBehaviorEntity userBehavior = userBehaviorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User behavior not found."));

        userBehavior.setBanned(true);
        userBehaviorRepository.save(userBehavior);

        // Additional logic to disable or delete the user account
        // userRepository.deleteById(userId);
    }

    private UserBehaviorEntity createUserBehavior(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        UserBehaviorEntity userBehavior = UserBehaviorEntity.builder()
                .user(user)
                .build();
        return userBehaviorRepository.save(userBehavior);
    }
}
