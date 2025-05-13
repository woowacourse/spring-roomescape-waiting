package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(final MemberRepository memberRepository, final JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(final LoginRequest loginRequest) {
        final MemberEmail email = new MemberEmail(loginRequest.email());
        final String password = loginRequest.password();
        final Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("올바르지 않은 로그인 정보입니다."));
        final Long id = member.getId();
        final String token = jwtTokenProvider.createToken(id.toString());
        return new TokenResponse(token);
    }

    public LoginCheckResponse checkMember(final String token) {
        final long id = jwtTokenProvider.getId(token);
        final Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("확인할 수 없는 사용자입니다."));
        return new LoginCheckResponse(member.getName());
    }
}
