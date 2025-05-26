package roomescape.auth.login.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.exception.UnauthorizedException;
import roomescape.auth.login.presentation.dto.LoginRequest;
import roomescape.auth.token.JwtTokenManager;
import roomescape.common.BaseServiceTest;

class LoginServiceTest extends BaseServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @DisplayName("어드민 로그인이 가능하다.")
    @Test
    void can_login_admin() {
        LoginRequest adminRequest = new LoginRequest("admin@email.com", "password");
        String adminToken = jwtTokenManager.createToken(1L, "ADMIN");

        String token = loginService.loginAdmin(adminRequest);

        Assertions.assertThat(token).isEqualTo(adminToken);
    }

    @DisplayName("어드민 로그인 시 비밀번호가 맞지 않으면 예외가 발생한다.")
    @Test
    void admin_login_not_match_password() {
        LoginRequest adminRequest = new LoginRequest("admin@email.com", "notPassword");

        Assertions.assertThatThrownBy(() -> loginService.loginAdmin(adminRequest))
            .isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("어드민 로그인 시 계정이 존재하지 않으면 예외가 발생한다.")
    @Test
    void admin_login_not_exists_account() {
        LoginRequest adminRequest = new LoginRequest("noAdmin@email.com", "notPassword");

        Assertions.assertThatThrownBy(() -> loginService.loginAdmin(adminRequest))
            .isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("멤버 로그인이 가능하다.")
    @Test
    void can_login_member() {
        LoginRequest memberRequest = new LoginRequest("member1@email.com", "password");
        String memberToken = jwtTokenManager.createToken(1L, "MEMBER");

        String token = loginService.loginMember(memberRequest);

        Assertions.assertThat(token).isEqualTo(memberToken);
    }

    @DisplayName("멤버 로그인 시 비밀번호가 맞지 않으면 예외가 발생한다.")
    @Test
    void member_login_not_match_password() {
        LoginRequest memberRequest = new LoginRequest("member1@email.com", "notPassword");

        Assertions.assertThatThrownBy(() -> loginService.loginMember(memberRequest))
            .isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("멤버 로그인 시 계정이 존재하지 않으면 예외가 발생한다.")
    @Test
    void member_login_not_exists_account() {
        LoginRequest memberRequest = new LoginRequest("noMember@email.com", "password");

        Assertions.assertThatThrownBy(() -> loginService.loginMember(memberRequest))
            .isInstanceOf(UnauthorizedException.class);
    }
}
