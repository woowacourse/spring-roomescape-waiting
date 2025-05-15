package roomescape.util;

import roomescape.member.domain.Member;

public interface TokenProvider {

    String createToken(Member member);

    Long getMemberIdFromToken(String token);
}
