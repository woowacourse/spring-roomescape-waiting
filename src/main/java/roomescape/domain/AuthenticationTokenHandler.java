package roomescape.domain;

public interface AuthenticationTokenHandler {

    String createToken(AuthenticationInfo authenticationInfo);

    long extractId(String token);

    AuthenticationInfo extractAuthenticationInfo(String token);

    boolean isValidToken(String token);
}
