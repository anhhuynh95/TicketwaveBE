package nl.fontys.s3.ticketwave_s3.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessToken;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenDecoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Exception.InvalidAccessTokenException;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp.AccessTokenImpl;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginResponse;
import nl.fontys.s3.ticketwave_s3.Controller.InterfaceService.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import nl.fontys.s3.ticketwave_s3.Controller.DTOS.LoginRequest;

import java.util.Arrays;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AccessTokenDecoder accessTokenDecoder;
    private final AccessTokenEncoder accessTokenEncoder;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(loginRequest);

        // Set HttpOnly cookie for access token
        response.addHeader("Set-Cookie", String.format(
                "jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                loginResponse.getAccessToken(),
                30 * 60 // 30 minutes
        ));

        // Set HttpOnly cookie for refresh token
        response.addHeader("Set-Cookie", String.format(
                "refreshToken=%s; HttpOnly; Path=/auth/refresh; Max-Age=%d; SameSite=Lax",
                loginResponse.getRefreshToken(),
                7 * 24 * 60 * 60 // 7 days
        ));

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the jwt cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);

        // Clear the refreshToken cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/auth/refresh");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);

        // Add cookies to the response
        response.addCookie(jwtCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // Extract the refresh token from the cookie
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token missing");
        }

        try {
            // Decode and verify the refresh token
            AccessToken decodedRefreshToken = accessTokenDecoder.decode(refreshToken);

            // Generate a new access token
            String newAccessToken = accessTokenEncoder.encode(
                    new AccessTokenImpl(
                            decodedRefreshToken.getSubject(),
                            decodedRefreshToken.getUserId(),
                            decodedRefreshToken.getRoles()
                    )
            );

            // Set a new access token in HttpOnly cookie
            response.addHeader("Set-Cookie", String.format(
                    "jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                    newAccessToken,
                    30 * 60 // 30 minutes
            ));

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
    }
}
