package nl.fontys.s3.ticketwave_s3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:testdb"
        }
)
@ActiveProfiles("test")
class TicketwaveApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context loads successfully
    }
}

