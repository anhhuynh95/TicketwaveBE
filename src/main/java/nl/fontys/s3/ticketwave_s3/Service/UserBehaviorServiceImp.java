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
    private final NotificationService notificationService;

    @Override
    public UserBehaviorDTO getUserBehavior(Integer userId) {
        return userBehaviorRepository.findByUserId(userId)
                .map(userBehaviorMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User behavior not found."));
    }

    @Override
    public void warnUser(Integer userId) {
        UserBehaviorEntity userBehavior = userBehaviorRepository.findByUserId(userId)
                .orElseGet(() -> createUserBehavior(userId));

        if (userBehavior.isBanned()) {
            throw new IllegalStateException("User is already banned.");
        }

        userBehavior.setWarnings(userBehavior.getWarnings() + 1);
        userBehaviorRepository.save(userBehavior);

        notificationService.notifyUser(userId, "You have been warned by an admin. Please adhere to community guidelines.");
    }

    public void banUser(Integer userId) {
        UserBehaviorEntity userBehavior = userBehaviorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User behavior not found."));

        userBehavior.setWarnings(2);
        userBehavior.setBanned(true);
        userBehaviorRepository.save(userBehavior);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setActive(false);
        userRepository.save(user);

        notificationService.notifyUser(userId, "Your account has been banned due to repeated violations.");
    }

    private UserBehaviorEntity createUserBehavior(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return userBehaviorRepository.save(
                UserBehaviorEntity.builder()
                        .user(user)
                        .warnings(0)
                        .isBanned(false)
                        .build()
        );
    }
}
