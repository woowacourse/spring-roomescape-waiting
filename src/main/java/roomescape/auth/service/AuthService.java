package roomescape.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.member.domain.Member;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthorizationProvider authorizationProvider;

    public AuthorizationPrincipal createMemberPrincipal(Member member) {
        return authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(member));
    }
}
