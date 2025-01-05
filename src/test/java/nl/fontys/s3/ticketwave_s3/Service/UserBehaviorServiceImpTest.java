package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Mapper.UserBehaviorMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserBehaviorRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBehaviorServiceImpTest {

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserBehaviorServiceImp userBehaviorService;

    @Test
    void getUserBehavior_shouldReturnBehavior_whenBehaviorExists() {

        Integer userId = 1;
        UserBehaviorEntity behaviorEntity = UserBehaviorEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .warnings(1)
                .isBanned(false)
                .build();
        UserBehaviorDTO behaviorDTO = UserBehaviorDTO.builder().userId(userId).warnings(1).isBanned(false).build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(behaviorEntity));
        when(userBehaviorMapper.toDTO(behaviorEntity)).thenReturn(behaviorDTO);

        UserBehaviorDTO result = userBehaviorService.getUserBehavior(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userBehaviorRepository).findByUserId(userId);
        verify(userBehaviorMapper).toDTO(behaviorEntity);
    }

    @Test
    void getUserBehavior_shouldThrowException_whenBehaviorNotFound() {
        Integer userId = 1;
        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userBehaviorService.getUserBehavior(userId));
        assertEquals("User behavior not found.", exception.getMessage());
    }

    @Test
    void warnUser_shouldIncreaseWarningsAndSendNotification() {
        Integer userId = 1;
        UserBehaviorEntity behaviorEntity = UserBehaviorEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .warnings(0)
                .isBanned(false)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(behaviorEntity));

        userBehaviorService.warnUser(userId);

        assertEquals(1, behaviorEntity.getWarnings());
        verify(userBehaviorRepository).save(behaviorEntity);
        verify(notificationService).notifyUser(userId, "You have been warned by an admin. Please adhere to community guidelines.");
    }

    @Test
    void warnUser_shouldThrowException_whenUserIsBanned() {
        Integer userId = 1;
        UserBehaviorEntity behaviorEntity = UserBehaviorEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .warnings(2)
                .isBanned(true)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(behaviorEntity));

        Exception exception = assertThrows(IllegalStateException.class, () -> userBehaviorService.warnUser(userId));
        assertEquals("User is already banned.", exception.getMessage());
    }

    @Test
    void warnUser_shouldCreateBehavior_whenNoBehaviorExists() {
        Integer userId = 1;
        UserEntity user = UserEntity.builder().id(userId).build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userBehaviorRepository.save(any(UserBehaviorEntity.class)))
                .thenAnswer(invocation -> {
                    UserBehaviorEntity behavior = invocation.getArgument(0);
                    behavior.setWarnings(1); // Simulate incrementing warnings
                    return behavior;
                });

        userBehaviorService.warnUser(userId);

        verify(userRepository).findById(userId);
        verify(userBehaviorRepository, times(2)).save(any(UserBehaviorEntity.class)); // Two saves: create and update
        verify(notificationService).notifyUser(userId, "You have been warned by an admin. Please adhere to community guidelines.");
    }

    @Test
    void banUser_shouldBanUserAndSendNotification() {
        Integer userId = 1;
        UserEntity user = UserEntity.builder().id(userId).active(true).build();
        UserBehaviorEntity behaviorEntity = UserBehaviorEntity.builder()
                .user(user)
                .warnings(1)
                .isBanned(false)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(behaviorEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userBehaviorService.banUser(userId);

        assertTrue(behaviorEntity.isBanned());
        assertEquals(2, behaviorEntity.getWarnings());
        assertFalse(user.isActive());
        verify(userBehaviorRepository).save(behaviorEntity);
        verify(userRepository).save(user);
        verify(notificationService).notifyUser(userId, "Your account has been banned due to repeated violations.");
    }

    @Test
    void banUser_shouldThrowException_whenBehaviorNotFound() {
        Integer userId = 1;
        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userBehaviorService.banUser(userId));
        assertEquals("User behavior not found.", exception.getMessage());
    }

}
