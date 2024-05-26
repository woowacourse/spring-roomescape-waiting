package roomescape.auth.core.token;

import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;

public interface TokenProvider {

    String createToken(Member member);

    AuthInfo extractAuthInfo(String token);
}
