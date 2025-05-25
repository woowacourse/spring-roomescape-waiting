package roomescape.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.auth.dto.LoginCommand;
import roomescape.application.auth.dto.LoginResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.infrastructure.error.exception.LoginAuthException;
import roomescape.infrastructure.security.JwtProperties;
import roomescape.infrastructure.security.JwtProvider;

class LoginServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    private LoginService loginService;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                new JwtProperties(
                        "yJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.ih1aovtQShabQ7l0cINw4k1fagApg3qLWiB8Kt59Lno",
                        Duration.ofHours(1L)
                ),
                clock);
        loginService = new LoginService(memberRepository, jwtProvider);
    }

    @Test
    void 사용자는_로그인을_할_수_있다() {
        // given
        memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        LoginCommand loginCommand = new LoginCommand("test@email.com", "pw");

        // when
        LoginResult loginResult = loginService.login(loginCommand);

        // then
        assertThat(loginResult)
                .isEqualTo(new LoginResult(jwtProvider.issue(1L).value()));
    }

    @Test
    void 로그인시_이메일에_해당하는_사용자가_없는경우_예외가_발생한다() {
        // given
        LoginCommand loginCommand = new LoginCommand("invalid@email.com", "pw");

        // when
        // then
        assertThatCode(() -> loginService.login(loginCommand))
                .isInstanceOf(LoginAuthException.class)
                .hasMessage("invalid@email.com에 해당하는 멤버가 존재하지 않습니다.");
    }

    @Test
    void 로그인시_비밀번호가_틀린경우_예외가_발생한다() {
        // given
        memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        LoginCommand loginCommand = new LoginCommand("test@email.com", "invalidpw");

        // when
        // then
        assertThatCode(() -> loginService.login(loginCommand))
                .isInstanceOf(LoginAuthException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
