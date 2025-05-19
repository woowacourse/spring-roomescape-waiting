package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
public class AuthServiceFacade {

    private final MemberService memberService;
    private final AuthorizationProvider authorizationProvider;

    public AuthServiceFacade(
        MemberService memberService,
        AuthorizationProvider authorizationProvider
    ) {
        this.memberService = memberService;
        this.authorizationProvider = authorizationProvider;
    }

    public AuthorizationPrincipal login(LoginRequest request) {
        Member member = memberService.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("인증되지 않은 유저 정보입니다."));
        if (checkInvalidLogin(member, request)) {
            throw new UnauthorizedException("인증되지 않은 유저 정보입니다.");
        }
        return authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(member));
    }

    public boolean checkInvalidLogin(Member member, LoginRequest request) {
        return member.checkInvalidLogin(request.email(), request.password());
    }

    public void validateMemberExistence(MemberPrincipal memberPrincipal) {
        if (!memberService.existsByName(memberPrincipal.name())) {
            throw new NotFoundException("존재하지 않는 유저 정보입니다.");
        }
    }
}
