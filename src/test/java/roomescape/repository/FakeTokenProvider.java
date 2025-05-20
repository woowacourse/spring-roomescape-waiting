package roomescape.repository;

import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
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
            throw new UnauthorizedException(ExceptionCause.JWT_TOKEN_INVALID);
        }
        return 1L;
    }
}
