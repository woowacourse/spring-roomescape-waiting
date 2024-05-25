package roomescape.security.provider;

public interface TokenProvider {

    String createToken(String subject);

    String extractSubject(String token);
}
