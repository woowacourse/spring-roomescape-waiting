package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.ForbiddenException;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
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
        final Password password = new Password(loginRequest.password());
        final Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("올바르지 않은 로그인 정보입니다."));
        final String token = jwtTokenProvider.createToken(member.getId(), member.getRole(),
                member.getName().getValue());
        return new TokenResponse(token);
    }

    public LoginMember checkMember(final String token) {
        jwtTokenProvider.validateToken(token);
        final long id = jwtTokenProvider.getId(token);
        final Role role = jwtTokenProvider.getRole(token);
        final String name = jwtTokenProvider.getName(token);
        return new LoginMember(id, role, name);
    }

    public void checkAdminMember(final String token) {
        jwtTokenProvider.validateToken(token);
        final Role role = jwtTokenProvider.getRole(token);
        if (!role.isAdmin()) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
    }
}
