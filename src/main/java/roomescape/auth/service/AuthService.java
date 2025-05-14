package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.service.out.TokenProvider;
import roomescape.auth.web.controller.request.LoginRequest;
import roomescape.auth.web.controller.response.MemberNameResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    public String login(LoginRequest request) {
        Member member = memberService.getMember(request.email(), request.password());

        return tokenProvider.issue(member);
    }

    public MemberNameResponse checkLogin(Long memberId) {
        Member member = memberService.getMember(memberId);

        return new MemberNameResponse(member.getName());
    }

    public boolean isAdmin(String token) {
        Role role = tokenProvider.getRole(token);
        return role.isAdmin();
    }

    public Long getMemberId(String token) {
        return tokenProvider.getMemberId(token);
    }
}
