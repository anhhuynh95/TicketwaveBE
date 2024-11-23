package nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp;

import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

// Implementation of AccessToken interface, which represents a JWT token
public class AccessTokenImpl implements AccessToken {
    private final String subject;
    private final Long userId;
    private final Set<String> roles;

    public AccessTokenImpl(String subject, Long userId, Collection<String> roles) {
        this.subject = subject;
        this.userId = userId;
        this.roles = roles != null ? Set.copyOf(roles) : Collections.emptySet();
    }

    @Override
    public String getSubject() { return subject; }

    @Override
    public Set<String> getRoles() { return roles; }

    @Override
    public Long getUserId() { return userId; }
}
