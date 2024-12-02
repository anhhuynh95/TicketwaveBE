package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Domain.User;

import java.util.Optional;

public interface UserService {
    User registerUser(UserDTO userDTO);
    LoginResponse login(LoginRequest loginRequest);
    Optional<User> findByUsername(String username);
    String findUsernameById(Integer userId);
}
