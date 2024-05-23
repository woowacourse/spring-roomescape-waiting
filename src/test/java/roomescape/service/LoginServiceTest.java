package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.WRONG_PASSWORD;

import io.jsonwebtoken.Claims;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.dto.LoginMemberRequest;
import roomescape.dto.LoginRequest;
import roomescape.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class LoginServiceTest extends FixtureUsingTest {

    @Autowired
    private JwtGenerator JWT_GENERATOR;
    @Autowired
    private LoginService loginService;

    @DisplayName("정상적인 로그인에 대해 토큰을 생성할 수 있다.")
    @Test
    void createTokenTest() {
        //when
        String createdToken = loginService.getLoginToken(new LoginRequest(
                USER.getEmail().getValue(),
                USER.getPassword().getValue()
        ));

        //then
        Claims payload = JWT_GENERATOR.getClaims(createdToken);
        assertAll(
                () -> assertThat(payload.get("id", Long.class)).isEqualTo(USER.getId()),
                () -> assertThat(payload.get("name")).isEqualTo(USER.getName().getValue())
        );
    }

    @DisplayName("존재하지 않는 email 요청을 하면 예외가 발생한다.")
    @Test
    void notFoundEmailGetTokenTest() {
        assertThatThrownBy(() -> loginService.getLoginToken(new LoginRequest(
                "wrongEmail@email.com",
                USER.getPassword().getValue()
        )))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("잘못된 비밀번호로 요청을 하면 예외가 발생한다.")
    @Test
    void illegalPasswordGetTokenTest() {
        assertThatThrownBy(() -> loginService.getLoginToken(new LoginRequest(
                USER.getEmail().getValue(),
                USER.getPassword() + " wrong"
        )))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(WRONG_PASSWORD.getMessage());
    }

    @DisplayName("로그인이 되었을 때 토큰으로 사용자 이름을 찾을 수 있다.")
    @Test
    void loginTokenContainsUserNameTest() {
        //given
        String token = JWT_GENERATOR.generateWith(Map.of(
                "id", USER.getId(),
                "name", USER.getName().getValue(),
                "role", USER.getRole().getTokenValue()
        ));

        //when
        LoginMemberRequest loginMemberRequest = loginService.checkLogin(token);

        //then
        assertThat(loginMemberRequest.name())
                .isEqualTo(USER.getName());
    }
}
