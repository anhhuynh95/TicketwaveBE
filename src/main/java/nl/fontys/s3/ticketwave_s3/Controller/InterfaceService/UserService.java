package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserDTO;
import nl.fontys.s3.ticketwave_s3.Domain.User;

public interface UserService {
    User registerUser(UserDTO userDTO);
}
