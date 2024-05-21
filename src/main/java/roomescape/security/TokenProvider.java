package roomescape.security;

public interface TokenProvider {

    String createToken(String memberId);

    Long getMemberId(String token);
}
