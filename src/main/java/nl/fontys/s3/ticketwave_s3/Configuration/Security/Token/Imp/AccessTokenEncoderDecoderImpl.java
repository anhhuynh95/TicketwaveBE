package nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Imp;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessToken;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenDecoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.AccessTokenEncoder;
import nl.fontys.s3.ticketwave_s3.Configuration.Security.Token.Exception.InvalidAccessTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Service for encoding and decoding JWT tokens
@Service
public class AccessTokenEncoderDecoderImpl implements AccessTokenEncoder, AccessTokenDecoder {
    private final Key key; // Cryptographic key for signing and verifying tokens

    // Inject secret key from application properties
    public AccessTokenEncoderDecoderImpl(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String encode(AccessToken accessToken) {
        // Map additional token claims
        Map<String, Object> claimsMap = new HashMap<>();
        if (accessToken.getUserId() != null) {
            claimsMap.put("userId", accessToken.getUserId());
        }
        if (!CollectionUtils.isEmpty(accessToken.getRoles())) {
            claimsMap.put("roles", accessToken.getRoles());
        }

        // Set token expiration time
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(accessToken.getSubject()) // Add username
                .setIssuedAt(Date.from(now)) // Add issue time
                .setExpiration(Date.from(now.plus(2, ChronoUnit.HOURS))) // Add expiration time
                .addClaims(claimsMap) // Add custom claims
                .signWith(key) // Sign with the cryptographic key
                .compact(); // Build the token
    }

    @Override
    public AccessToken decode(String accessTokenEncoded) {
        try {
            // Parse and validate the token
            Jwt<?, Claims> jwt = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(accessTokenEncoded);
            Claims claims = jwt.getBody();

            // Extract claims from the token
            Long userId = claims.get("userId", Long.class);
            List<String> roles = claims.get("roles", List.class);

            // Return decoded token as AccessTokenImpl
            return new AccessTokenImpl(claims.getSubject(), userId, roles);
        } catch (JwtException e) {
            throw new InvalidAccessTokenException(e.getMessage());
        }
    }
}
