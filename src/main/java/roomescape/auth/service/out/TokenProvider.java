package roomescape.auth.service.out;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public interface TokenProvider {

    String issue(Member member);

    Role getRole(String token);

    Long getMemberId(String token);
}
