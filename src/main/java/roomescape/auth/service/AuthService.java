package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;

@Service
public class AuthService {

    private final AuthorizationProvider authorizationProvider;

    public AuthService(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
    }

    public AuthorizationPrincipal login(Member member, LoginRequest request) {
        validateInvalidLogin(member, request);
        return authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(member));
    }

    private void validateInvalidLogin(Member member, LoginRequest request) {
        if (!member.hasSameEmail(request.email()) || !member.hasSamePassword(request.password())) {
            throw new UnauthorizedException("이메일 혹은 비밀번호가 일치하지 않습니다.");
        }
    }
}
