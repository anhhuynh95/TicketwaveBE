package nl.fontys.s3.ticketwave_s3.Configuration.Security.Token;

import java.util.Set;

public interface AccessToken {
    String getSubject();
    Set<String> getRoles();
    Long getUserId();
}
