package roomescape.common.security.application;

import org.springframework.stereotype.Component;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.common.security.dto.response.LoginResponse;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.exception.UnAuthorizedException;
import roomescape.common.security.infrastructure.JwtProvider;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.JpaMemberRepository;

@Component
public class AuthService {

    private final JwtProvider jwtProvider;
    private final JpaMemberRepository memberRepository;
    private final MyPasswordEncoder myPasswordEncoder;

    public AuthService(final JwtProvider jwtProvider, final JpaMemberRepository memberRepository,
                       final MyPasswordEncoder myPasswordEncoder) {
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
        this.myPasswordEncoder = myPasswordEncoder;
    }

    public LoginResponse login(final LoginRequest loginRequest) {
        Member member = findValidMember(loginRequest.email(), loginRequest.password());
        String accessToken = jwtProvider.createToken(MemberInfo.from(member));
        return new LoginResponse(accessToken);
    }

    private Member findValidMember(final String email, final String password) {
        Member member = findMemberByEmail(email);
        checkPassword(password, member);
        return member;
    }

    public MemberInfo makeMemberInfo(final String token) {
        validateToken(token);
        Long memberId = jwtProvider.getMemberId(token);
        return new MemberInfo(memberId, jwtProvider.getRole(token));
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UnAuthorizedException("존재하지 않은 사용자입니다."));
    }

    private void validateToken(final String token) {
        if (jwtProvider.isInvalidToken(token)) {
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    private void checkPassword(final String password, final Member member) {
        if (!myPasswordEncoder.matches(password, member.getPassword())) {
            throw new UnAuthorizedException("로그인에 실패하였습니다.");
        }
    }
}
