package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;
import nl.fontys.s3.ticketwave_s3.Mapper.UserBehaviorMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserBehaviorEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserBehaviorRepository;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBehaviorServiceImpTest {

    @InjectMocks
    private UserBehaviorServiceImp userBehaviorService;

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBehaviorMapper userBehaviorMapper;

    @Test
    void getUserBehavior_shouldReturnUserBehaviorDTO() {
        Integer userId = 1;

        UserBehaviorEntity mockEntity = UserBehaviorEntity.builder()
                .id(1)
                .warnings(0)
                .isBanned(false)
                .build();

        UserBehaviorDTO mockDTO = UserBehaviorDTO.builder()
                .warnings(0)
                .isBanned(false)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(mockEntity));
        when(userBehaviorMapper.toDTO(mockEntity)).thenReturn(mockDTO);

        UserBehaviorDTO result = userBehaviorService.getUserBehavior(userId);

        assertNotNull(result);
        assertEquals(mockDTO, result);
    }

    @Test
    void getUserBehavior_shouldThrowExceptionWhenNotFound() {
        Integer userId = 1;

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userBehaviorService.getUserBehavior(userId));

        assertEquals("User behavior not found.", exception.getMessage());
    }

    @Test
    void warnUser_shouldIncreaseWarnings() {
        Integer userId = 1;

        UserBehaviorEntity mockEntity = UserBehaviorEntity.builder()
                .id(1)
                .warnings(0)
                .isBanned(false)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(mockEntity));
        when(userBehaviorRepository.save(mockEntity)).thenReturn(mockEntity);

        userBehaviorService.warnUser(userId);

        assertEquals(1, mockEntity.getWarnings());
    }

    @Test
    void banUser_shouldBanUser() {
        Integer userId = 1;

        UserBehaviorEntity mockEntity = UserBehaviorEntity.builder()
                .id(1)
                .warnings(2)
                .isBanned(false)
                .build();

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.of(mockEntity));

        userBehaviorService.banUser(userId);

        assertTrue(mockEntity.isBanned());
        verify(userBehaviorRepository).save(mockEntity);
    }

    @Test
    void banUser_shouldThrowExceptionWhenNotFound() {
        Integer userId = 1;

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userBehaviorService.banUser(userId));

        assertEquals("User behavior not found.", exception.getMessage());
    }
}
