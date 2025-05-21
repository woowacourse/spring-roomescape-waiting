package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
public class AuthServiceFacade {

    private final AuthService authService;
    private final MemberService memberService;

    public AuthServiceFacade(
        AuthService authService,
        MemberService memberService
    ) {
        this.authService = authService;
        this.memberService = memberService;
    }

    public AuthorizationPrincipal login(LoginRequest request) {
        Member member = memberService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("인증되지 않은 유저 정보입니다."));

        return authService.login(member, request);
    }

    public void validateMemberExistence(MemberPrincipal memberPrincipal) {
        if (!memberService.existsByName(memberPrincipal.name())) {
            throw new NotFoundException("존재하지 않는 유저 정보입니다.");
        }
    }
}
