package roomescape.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.auth.dto.LoginParam;
import roomescape.application.auth.dto.LoginResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.infrastructure.error.exception.LoginAuthException;
import roomescape.infrastructure.security.JwtProperties;
import roomescape.infrastructure.security.JwtProvider;

class AuthServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    private AuthService authService;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                new JwtProperties(
                        "yJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.ih1aovtQShabQ7l0cINw4k1fagApg3qLWiB8Kt59Lno",
                        Duration.ofHours(1L)
                ),
                clock);
        authService = new AuthService(memberRepository, jwtProvider);
    }

    @Test
    void 사용자는_로그인을_할_수_있다() {
        // given
        memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        LoginParam loginParam = new LoginParam("test@email.com", "pw");

        // when
        LoginResult loginResult = authService.login(loginParam);

        // then
        assertThat(loginResult)
                .isEqualTo(new LoginResult(jwtProvider.issue(1L).value()));
    }

    @Test
    void 로그인시_이메일에_해당하는_사용자가_없는경우_예외가_발생한다() {
        // given
        LoginParam loginParam = new LoginParam("invalid@email.com", "pw");

        // when
        // then
        assertThatCode(() -> authService.login(loginParam))
                .isInstanceOf(LoginAuthException.class)
                .hasMessage("invalid@email.com에 해당하는 멤버가 존재하지 않습니다.");
    }

    @Test
    void 로그인시_비밀번호가_틀린경우_예외가_발생한다() {
        // given
        memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        LoginParam loginParam = new LoginParam("test@email.com", "invalidpw");

        // when
        // then
        assertThatCode(() -> authService.login(loginParam))
                .isInstanceOf(LoginAuthException.class)
                .hasMessage("test@email.com 사용자의 비밀번호가 같지 않습니다.");
    }
}
