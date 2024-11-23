package nl.fontys.s3.ticketwave_s3.Service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCredentialsException extends ResponseStatusException {
    public InvalidCredentialsException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS");
    }
}
