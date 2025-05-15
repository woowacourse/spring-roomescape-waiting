package roomescape.global.auth.service;

import org.springframework.stereotype.Component;
import roomescape.global.auth.dto.LoginRequest;
import roomescape.global.auth.dto.LoginResponse;
import roomescape.global.auth.dto.UserInfo;
import roomescape.global.auth.exception.UnAuthorizedException;
import roomescape.global.auth.infrastructure.JwtProvider;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Component
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public AuthService(final JwtProvider jwtProvider, final MemberRepository memberRepository) {
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }

    public LoginResponse createToken(final LoginRequest loginRequest) {
        Member member = getUserInfoByEmailAndPassword(loginRequest.email(), loginRequest.password());
        String accessToken = jwtProvider.createToken(UserInfo.from(member));
        return new LoginResponse(accessToken);
    }

    public UserInfo makeUserInfo(final String token) {
        validateToken(token);
        Long memberId = jwtProvider.getMemberId(token);
        return new UserInfo(memberId, jwtProvider.getName(token), jwtProvider.getRole(token));
    }

    private void validateToken(final String token) {
        if (jwtProvider.isInvalidToken(token)) {
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    private Member getUserInfoByEmailAndPassword(final String email, final String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnAuthorizedException("존재하지 않은 사용자입니다."));
    }
}
