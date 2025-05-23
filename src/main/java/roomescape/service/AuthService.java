package roomescape.service;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberService memberService;

    public Long authenticate(final LoginRequest loginRequest) {
        final Member member = memberService.getMemberByEmail(loginRequest.email());

        if (!matches(loginRequest.password(), member.getPassword())) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 일치 하지않습니다.");
        }
        return member.getId();
    }

    public void updateSessionIdByMemberId(final Long memberId, final String sessionId) {
        final Member member = memberService.getMemberById(memberId);

        member.updateSessionId(sessionId);
    }

    private String encode(final String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    private boolean matches(final String rawPassword, final String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

}
