package nl.fontys.s3.ticketwave_s3.Controller.DTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Domain.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Integer id;

    @NotBlank(message = "Username must not be blank")
    @Email(message = "Username must be a valid email")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @Builder.Default
    private UserRole role = UserRole.USER;
}
