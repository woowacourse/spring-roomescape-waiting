package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.MemberRole;
import roomescape.exception.login.UnauthorizedEmailException;
import roomescape.exception.login.UnauthorizedPasswordException;
import roomescape.service.login.JwtTokenProvider;
import roomescape.service.login.LoginService;
import roomescape.service.login.dto.LoginCheckResponse;
import roomescape.service.login.dto.LoginRequest;

public class LoginServiceTest extends ServiceTest {
    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        void 이메일과_비밀번호로_로그인할_수_있다() {
            LoginRequest request = new LoginRequest("admin@gmail.com", "1234567890");
            assertThatCode(() -> loginService.login(request))
                    .doesNotThrowAnyException();
        }

        @Test
        void 이메일이_틀리면_로그인할_수_없다() {
            LoginRequest request = new LoginRequest("wrong@gmail.com", "1234567890");
            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(UnauthorizedEmailException.class);
        }

        @Test
        void 비밀번호가_틀리면_로그인할_수_없다() {
            LoginRequest request = new LoginRequest("admin@gmail.com", "wrongpassword");
            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(UnauthorizedPasswordException.class);
        }
    }

    @Nested
    @DisplayName("인증 정보(로그인한 사용자 정보) 조회")
    class LoginCheck {
        @Test
        void 로그인한_사용자_정보를_조회할_수_있다() {
            Member member = new Member(
                    1L,
                    new MemberName("사용자"),
                    new MemberEmail("user@gmail.com"),
                    new MemberPassword("1234567890"),
                    MemberRole.USER
            );
            LoginCheckResponse response = loginService.loginCheck(member);
            assertThat(response.getName()).isEqualTo("사용자");
        }
    }

    @Nested
    @DisplayName("토큰으로 사용자 정보 조회")
    class findByToken {
        private MemberEmail email;
        private MemberRole role;
        private String token;

        @BeforeEach
        void setUp() {
            email = new MemberEmail("user@gmail.com");
            role = MemberRole.USER;
            token = jwtTokenProvider.createToken(email, role);
        }

        @Test
        void 토큰으로_사용자_정보를_조회할_수_있다() {
            Member member = new Member(1L, new MemberName("사용자"), email, new MemberPassword("1234567890"), role);
            assertThat(loginService.findMemberByToken(token))
                    .isEqualTo(member);
        }

        @Test
        void 토큰으로_사용자_역할을_조회할_수_있다() {
            assertThat(loginService.findMemberRoleByToken(token))
                    .isEqualTo(role);
        }
    }
}
