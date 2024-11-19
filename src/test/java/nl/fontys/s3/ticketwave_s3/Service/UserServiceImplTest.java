package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Service.InterfaceRepo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_shouldRegisterUserSuccessfully() {
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .password("password")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .username("testuser")
                .password("encodedPassword")
                .build();

        User expectedUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMapper.toEntity(userDTO)).thenReturn(userEntity);
        when(userMapper.toDomain(userEntity)).thenReturn(expectedUser);

        User result = userService.registerUser(userDTO);

        assertEquals(expectedUser, result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userEntity);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void registerUser_shouldThrowExceptionWhenUsernameExists() {
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .password("password")
                .build();

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(User.builder().username("testuser").build()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(userDTO)
        );

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldEncodePasswordBeforeSaving() {
        UserDTO userDTO = UserDTO.builder()
                .username("secureuser")
                .password("securepassword")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .username("secureuser")
                .password("encodedSecurePassword")
                .build();

        when(userRepository.findByUsername("secureuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securepassword")).thenReturn("encodedSecurePassword");
        when(userMapper.toEntity(userDTO)).thenReturn(userEntity);

        userService.registerUser(userDTO);

        verify(passwordEncoder).encode("securepassword");
        verify(userRepository).save(userEntity);
    }
}
