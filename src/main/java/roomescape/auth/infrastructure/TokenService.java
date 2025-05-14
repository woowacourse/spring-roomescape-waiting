package roomescape.auth.infrastructure;

import roomescape.auth.dto.AuthenticatedMember;
import roomescape.domain.member.model.Member;

public interface TokenService {

    String create(Member member);

    AuthenticatedMember resolveAuthenticatedMember(String token);
}
