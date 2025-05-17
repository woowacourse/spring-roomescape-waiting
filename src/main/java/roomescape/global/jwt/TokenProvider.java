package roomescape.global.jwt;

import roomescape.member.domain.Member;

public interface TokenProvider {
    String createToken(Member member);

    TokenInfo getInfo(String token);
}
