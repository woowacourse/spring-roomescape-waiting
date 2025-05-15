package roomescape.repository;

import roomescape.exception.InvalidAuthorizationException;
import roomescape.member.domain.Member;
import roomescape.util.TokenProvider;

public class FakeTokenProvider implements TokenProvider {

    @Override
    public String createToken(Member member) {
        return "admin@gmail.com";
    }

    @Override
    public Long getMemberIdFromToken(String token) {
        if (token.equals("invalid")) {
            throw new InvalidAuthorizationException("유효하지 않습니다.");
        }
        return 1L;
    }
}
