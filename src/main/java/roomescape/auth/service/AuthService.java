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
        final String password = loginRequest.password();
        final Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("올바르지 않은 로그인 정보입니다."));
        final Long id = member.getId();
        final String token = jwtTokenProvider.createToken(id.toString());
        return new TokenResponse(token);
    }

    public LoginMember checkMember(final String token) {
        final Member member = findMemberByToken(token);
        return new LoginMember(member);
    }

    public LoginMember checkAdminMember(final String token) {
        final Member member = findMemberByToken(token);
        if (!member.hasRole(Role.ADMIN)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        return new LoginMember(member);
    }

    private Member findMemberByToken(final String token) {
        final long id = jwtTokenProvider.getId(token);
        final Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("확인할 수 없는 사용자입니다."));
        return member;
    }
}
