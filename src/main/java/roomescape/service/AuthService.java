package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.global.PasswordEncoder;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public Long authenticate(final LoginRequest loginRequest) {
        final Member member = memberService.getMemberByEmail(loginRequest.email());

        if (!passwordEncoder.matches(loginRequest.password(), member.getPassword())) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 일치 하지않습니다.");
        }
        return member.getId();
    }

    public void updateSessionIdByMemberId(final Long memberId, final String sessionId) {
        final Member member = memberService.getMemberById(memberId);

        member.updateSessionId(sessionId);
    }
}
