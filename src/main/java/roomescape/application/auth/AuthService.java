package roomescape.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.auth.dto.JwtPayload;
import roomescape.application.auth.dto.LoginParam;
import roomescape.application.auth.dto.LoginResult;
import roomescape.infrastructure.error.exception.LoginAuthException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.infrastructure.security.JwtProvider;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public LoginResult login(LoginParam loginParam) {
        Member member = getMemberByEmail(new Email(loginParam.email()));
        if (member.isNotPassword(loginParam.password())) {
            throw new LoginAuthException(loginParam.email() + " 사용자의 비밀번호가 같지 않습니다.");
        }
        return createLoginResult(member);
    }

    private Member getMemberByEmail(Email email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new LoginAuthException(email.value() + "에 해당하는 멤버가 존재하지 않습니다."));
    }

    private LoginResult createLoginResult(Member member) {
        String accessToken = jwtProvider.issue(new JwtPayload(member.getId(), member.getName(), member.getRole()));
        return new LoginResult(accessToken);
    }
}
