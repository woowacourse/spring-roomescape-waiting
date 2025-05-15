package roomescape.repository;

import roomescape.domain.Member;
import roomescape.util.TokenProvider;

public class FakeTokenProvider implements TokenProvider {

    @Override
    public String createToken(Member member) {
        return "wooteco";
    }

    @Override
    public Long getMemberIdFromToken(String token) {
        return 1L;
    }
}
