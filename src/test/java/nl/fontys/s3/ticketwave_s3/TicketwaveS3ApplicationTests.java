package nl.fontys.s3.ticketwave_s3;

import nl.fontys.s3.ticketwave_s3.Configuration.Security.WebSecurityConfig;
import nl.fontys.s3.ticketwave_s3.Configuration.WebSocketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, WebSecurityConfig.class, WebSocketConfig.class})
class TicketwaveApplicationTests {

    @Test
    void contextLoads() {}
}
