package nl.fontys.s3.ticketwave_s3;

import nl.fontys.s3.ticketwave_s3.Configuration.CloudinaryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CloudinaryConfig.class)
public class TicketwaveS3Application {

    public static void main(String[] args) {
        SpringApplication.run(TicketwaveS3Application.class, args);
    }

}
