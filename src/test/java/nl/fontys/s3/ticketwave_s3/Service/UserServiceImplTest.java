package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.Exception.InvalidCredentialsException;
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
    private UserDBRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenEncoder accessTokenEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_shouldRegisterUserSuccessfully() {
        UserDTO userDTO = UserDTO.builder().username("testuser").password("password").build();
        UserEntity userEntity = UserEntity.builder()
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();
        User expectedUser = User.builder().username("testuser").password("encodedPassword").role(UserRole.USER).build();

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
        UserDTO userDTO = UserDTO.builder().username("testuser").password("password").build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new UserEntity()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(userDTO));

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnAccessTokenWhenCredentialsAreValid() {
        String username = "testuser";
        String password = "password";

        UserEntity userEntity = UserEntity.builder()
                .id(1)
                .username(username)
                .password("encodedPassword")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(accessTokenEncoder.encode(any())).thenReturn("access-token");

        LoginResponse result = userService.login(new LoginRequest(username, password));

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("MANAGER", result.getRole());
        verify(accessTokenEncoder).encode(any());
    }

    @Test
    void login_shouldThrowExceptionWhenCredentialsAreInvalid() {
        String username = "testuser";
        String password = "wrongpassword";

        UserEntity userEntity = UserEntity.builder()
                .id(1)
                .username(username)
                .password("encodedPassword")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(new LoginRequest(username, password)));

        verify(accessTokenEncoder, never()).encode(any());
    }

    @Test
    void findByUsername_shouldReturnUser_whenUserExists() {
        String username = "testuser";
        UserEntity userEntity = UserEntity.builder()
                .id(1)
                .username(username)
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();
        User user = User.builder().id(1).username(username).password("encodedPassword").role(UserRole.USER).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        Optional<User> result = userService.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByUsername(username);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional_whenUserNotFound() {
        String username = "unknownuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findUsernameById_shouldReturnUsername_whenUserExists() {
        Integer userId = 1;
        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .build();
        User user = User.builder().id(userId).username("testuser").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        String result = userService.findUsernameById(userId);

        assertEquals("testuser", result);
        verify(userRepository).findById(userId);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findUsernameById_shouldReturnUnknownUser_whenUserNotFound() {
        Integer userId = 999;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String result = userService.findUsernameById(userId);

        assertEquals("Unknown User", result);
        verify(userRepository).findById(userId);
    }
}
