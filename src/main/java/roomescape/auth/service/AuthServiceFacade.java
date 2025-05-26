package roomescape.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
@AllArgsConstructor
public class AuthServiceFacade {

    private final AuthService authService;
    private final MemberService memberService;

    public AuthorizationPrincipal login(LoginRequest request) {
        Member member = memberService.findByEmailAndPassword(request.email(), request.password())
            .orElseThrow(() -> new UnauthorizedException("이메일 혹은 비밀번호가 일치하지 않습니다."));

        return authService.createMemberPrincipal(member);
    }

    public void validateMemberExistence(MemberPrincipal memberPrincipal) {
        if (!memberService.existsByName(memberPrincipal.name())) {
            throw new NotFoundException("존재하지 않는 유저 정보입니다.");
        }
    }
}
