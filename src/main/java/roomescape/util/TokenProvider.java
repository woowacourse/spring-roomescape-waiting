package roomescape.util;

import roomescape.domain.Member;

public interface TokenProvider {

    String createToken(Member member);

    Long getMemberIdFromToken(String token);
}
