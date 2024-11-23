package nl.fontys.s3.ticketwave_s3.Configuration.Security.Auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessToken;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenDecoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Exception.InvalidAccessTokenException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private static final String SPRING_SECURITY_ROLE_PREFIX = "ROLE_";
    private static final String COOKIE_NAME = "jwt";

    private final AccessTokenDecoder accessTokenDecoder;

    public AuthenticationRequestFilter(AccessTokenDecoder accessTokenDecoder) {
        this.accessTokenDecoder = accessTokenDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Log cookies received in the request
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies).forEach(cookie ->
                    System.out.println("Cookie Name: " + cookie.getName() + ", Value: " + cookie.getValue())
            );
        }

        // Extract token from the cookie
        String accessTokenString = extractTokenFromCookie(request);
        System.out.println("Extracted JWT: " + accessTokenString); // Log extracted JWT
        if (accessTokenString == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Decode and validate the token
            AccessToken accessToken = accessTokenDecoder.decode(accessTokenString);
            System.out.println("Decoded JWT - Subject: " + accessToken.getSubject() +
                    ", Roles: " + accessToken.getRoles());
            setupSpringSecurityContext(accessToken);
            chain.doFilter(request, response);
        } catch (InvalidAccessTokenException e) {
            logger.error("Error validating access token", e);
            sendAuthenticationError(response);
        }
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        // Get cookies from the request
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // Look for the "jwt" cookie
        Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst();

        return jwtCookie.map(Cookie::getValue).orElse(null);
    }

    private void sendAuthenticationError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Invalid or missing access token\"}");
        response.flushBuffer();
    }

    private void setupSpringSecurityContext(AccessToken accessToken) {
        UserDetails userDetails = new User(accessToken.getSubject(), "",
                accessToken.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(SPRING_SECURITY_ROLE_PREFIX + role))
                        .toList());

        System.out.println("Security Context Authentication: " + SecurityContextHolder.getContext().getAuthentication());

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(accessToken);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

}
