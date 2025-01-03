package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp.AccessTokenImpl;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.Exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDBRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenEncoder accessTokenEncoder;
    private final UserMapper userMapper;

    @Override
    public User registerUser(UserDTO userDTO) {
        userRepository.findByUsername(userDTO.getUsername())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Username is already taken");
                });

        UserEntity userEntity = userMapper.toEntity(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(userEntity);
        return userMapper.toDomain(userEntity);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Your account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Generate access token
        String accessToken = accessTokenEncoder.encode(
                new AccessTokenImpl(
                        user.getUsername(),
                        Long.valueOf(user.getId()),
                        List.of(user.getRole().name())
                )
        );

        // Generate refresh token
        String refreshToken = accessTokenEncoder.encodeRefreshToken(
                new AccessTokenImpl(
                        user.getUsername(),
                        Long.valueOf(user.getId()),
                        List.of(user.getRole().name())
                )
        );


        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public String findUsernameById(Integer userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDomain)
                .map(User::getUsername)
                .orElse("Unknown User");
    }

    @Override
    public Integer findUserIdByEmail(String email) {
        return userRepository.findByUsername(email)
                .map(UserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
    }

}
