package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.Exception.InvalidCredentialsException;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
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

        var result = userService.login(new nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest(username, password));

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

        var loginRequest = new nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest(username, password);
        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));

        verify(accessTokenEncoder, never()).encode(any());
    }
}
