package roomescape.service.auth;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.exception.AuthenticationException;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.request.LoginRequest;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public AuthService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public Member findMemberByToken(String token) {
        long memberId = tokenProvider.parseToken(token);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("잘못된 토큰 정보입니다."));
    }

    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new AuthenticationException("잘못된 로그인 정보입니다."));

        return tokenProvider.generateAccessToken(member.getId());
    }
}
