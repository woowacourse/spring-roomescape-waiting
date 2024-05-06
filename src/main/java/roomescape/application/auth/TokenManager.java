package roomescape.application.auth;

import roomescape.domain.role.MemberRole;

public interface TokenManager {

    String createToken(MemberRole memberRole);

    MemberRole extract(String token);
}
