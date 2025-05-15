package roomescape.member.service;

public interface TokenProvider {

    String createToken(final String payload);

    String parsePayload(final String token);
}
