package nl.fontys.s3.ticketwave_s3.Configuration.Security.Token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
