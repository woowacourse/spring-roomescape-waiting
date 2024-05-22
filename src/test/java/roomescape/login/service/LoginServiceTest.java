package roomescape.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialMemberFixture.ADMIN;
import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.NOT_SAVED_MEMBER;

import javax.naming.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.NotFoundException;
import roomescape.login.dto.LoginRequest;
import roomescape.login.dto.TokenResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class LoginServiceTest {

    @Autowired
    private LoginService loginService;

    @Test
    @DisplayName("존재하지 않는 회원 email로 로그인을 시도할 경우 예외가 발생한다.")
    void throwExceptionIfNotExistEmail() {
        LoginRequest loginRequest = new LoginRequest(
                COMMON_PASSWORD.password(),
                NOT_SAVED_MEMBER.getEmail().email()
        );

        assertThatThrownBy(() -> loginService.createMemberToken(loginRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 회원 email이지만 틀린 비밀번호로 로그인을 시도할 경우 예외가 발생한다.")
    void throwExceptionIfInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest(
                COMMON_PASSWORD.password() + "123",
                MEMBER_1.getEmail().email()
        );

        assertThatThrownBy(() -> loginService.createMemberToken(loginRequest))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰을 발행한다.")
    void getTokenIfLoginSucceeds() throws AuthenticationException {
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), ADMIN.getEmail().email());

        TokenResponse tokenResponse = loginService.createMemberToken(loginRequest);

        assertThat(tokenResponse.token()).isNotNull();
    }
}
