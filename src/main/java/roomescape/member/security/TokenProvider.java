package roomescape.member.security;

public interface TokenProvider {

    String createToken(final String payload);

    String parsePayload(final String token);
}
