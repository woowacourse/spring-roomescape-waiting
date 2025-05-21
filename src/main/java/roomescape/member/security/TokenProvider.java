package roomescape.member.security;

public interface TokenProvider {

    String createToken(String payload);

    String parsePayload(String token);

    boolean validateToken(String token);
}
