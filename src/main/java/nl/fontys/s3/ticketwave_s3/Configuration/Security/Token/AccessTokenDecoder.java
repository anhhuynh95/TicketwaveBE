package nl.fontys.s3.ticketwave_s3.Configuration.Security.Token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
