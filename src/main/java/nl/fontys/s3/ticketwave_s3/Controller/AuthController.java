package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(loginRequest);

        // Manually set cookie with SameSite=None
        String cookieHeader = String.format(
                "jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=None; Secure",
                loginResponse.getAccessToken(), 30 * 60
        );
        response.addHeader("Set-Cookie", cookieHeader);

        return ResponseEntity.ok(loginResponse);
    }


}
