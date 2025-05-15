package roomescape.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.infrastructure.TokenService;
import roomescape.global.exception.AuthenticationException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    public String createAuthenticationToken(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new AuthenticationException("아이디 또는 비밀번호가 일치하지 않습니다."));
        return tokenService.create(member);
    }

    public AuthenticatedMember getAuthenticatedMember(String token) {
        return tokenService.resolveAuthenticatedMember(token);
    }
}
