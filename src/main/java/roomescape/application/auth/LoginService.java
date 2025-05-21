package roomescape.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.auth.dto.LoginCommand;
import roomescape.application.auth.dto.LoginResult;
import roomescape.infrastructure.error.exception.LoginAuthException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.infrastructure.security.AccessToken;
import roomescape.infrastructure.security.JwtProvider;

@Service
@Transactional(readOnly = true)
public class LoginService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public LoginService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public LoginResult login(LoginCommand loginCommand) {
        Member member = getMemberByEmail(loginCommand.email());
        validatePasswordMatch(member, loginCommand.password());
        return createLoginResult(member);
    }

    private void validatePasswordMatch(Member member, String password) {
        if (member.isNotPassword(password)) {
            throw new LoginAuthException("비밀번호가 일치하지 않습니다.");
        }
    }

    private Member getMemberByEmail(String emailValue) {
        return memberRepository.findByEmail(new Email(emailValue))
                .orElseThrow(() -> new LoginAuthException(emailValue + "에 해당하는 멤버가 존재하지 않습니다."));
    }

    private LoginResult createLoginResult(Member member) {
        AccessToken accessToken = jwtProvider.issue(member.getId());
        return new LoginResult(accessToken.value());
    }
}
