package nl.fontys.s3.ticketwave_s3.Configuration;

import nl.fontys.s3.ticketwave_s3.Configuration.Security.Auth.AuthenticationRequestFilter;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp.AccessTokenImpl;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenDecoder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@TestConfiguration
public class TestConfig {

    @Bean
    public AccessTokenDecoder accessTokenDecoder() {
        return token -> new AccessTokenImpl("test-user", 1L, Set.of("ROLE_USER"));
    }

    @Bean
    public AuthenticationRequestFilter authenticationRequestFilter() {
        return new AuthenticationRequestFilter(accessTokenDecoder());
    }
}
