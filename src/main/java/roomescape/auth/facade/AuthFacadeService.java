package roomescape.auth.facade;

import org.springframework.stereotype.Service;
import roomescape.auth.domain.Token;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginCheckResponse;
import roomescape.member.service.MemberService;

@Service
public class AuthFacadeService {

    private final AuthService authService;
    private final MemberService memberService;

    public AuthFacadeService(AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    public MemberLoginCheckResponse findLoginMemberInfo(long memberId) {
        Member member = memberService.findMember(memberId);

        return new MemberLoginCheckResponse(member.getName());
    }

    public Token login(LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
