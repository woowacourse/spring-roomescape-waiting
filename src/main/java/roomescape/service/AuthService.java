package roomescape.service;

import java.util.Base64;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.repository.MemberRepository;


@Service
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member getMemberByEmailAndPassword(final LoginRequest loginRequest) {
        final Member member = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 사용자를 찾을 수 없습니다."));
        if (!matches(loginRequest.password(), member.getPassword())) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 일치 하지않습니다.");
        }
        return member;
    }

    public void updateSessionId(final Member member, final String sessionId) {
        memberRepository.updateSessionId(member.getId(), sessionId);
    }

    private String encode(final String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    private boolean matches(final String rawPassword, final String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

}
