package nl.fontys.s3.ticketwave_s3.Service;

import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp.AccessTokenImpl;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import nl.fontys.s3.ticketwave_s3.Domain.User;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import nl.fontys.s3.ticketwave_s3.Service.Exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDBRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenEncoder accessTokenEncoder;
    private final UserMapper userMapper;

    @Override
    public User registerUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Convert DTO to Entity and hash password
        UserEntity userEntity = userMapper.toEntity(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Assign role: use provided role or default to USER
        if (userDTO.getRole() == null) { // Check if role is null
            userEntity.setRole(UserRole.USER); // Default role
        } else {
            userEntity.setRole(userDTO.getRole()); // Use provided role directly (already of type UserRole)
        }

        // Save user entity and return domain object
        userRepository.save(userEntity);
        return userMapper.toDomain(userEntity);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Fetch user from repository
        UserEntity user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        // Validate password
        if (!matchesPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generate access token
        String accessToken = generateAccessToken(user);

        // Return LoginResponse with accessToken and role
        return LoginResponse.builder()
                .accessToken(accessToken)
                .role(user.getRole().name()) // Include role in response
                .build();
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private String generateAccessToken(UserEntity user) {
        // Convert UserEntity fields to token payload
        return accessTokenEncoder.encode(new AccessTokenImpl(
                user.getUsername(),
                Long.valueOf(user.getId()), // Convert Integer to Long
                List.of(user.getRole().name()) // Convert single role to List<String>
        ));
    }
}
