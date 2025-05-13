package roomescape.login.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.Token;
import roomescape.global.exception.impl.NotFoundException;
import roomescape.login.presentation.request.LoginRequest;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LoginServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtHandler jwtHandler;

    @Test
    void 로그인_요청이_들어오면_토큰을_발급한다() {
        // given
        final LoginRequest request = new LoginRequest("test1@test.com", "1234");

        // when
        final Token token = loginService.login(request);
        final String accessToken = token.accessToken();
        final Map<String, String> claims = jwtHandler.decode(accessToken);

        // then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(accessToken).isNotEmpty(),
                () -> assertThat(claims).containsKey(JwtHandler.CLAIM_ID_KEY),
                () -> assertThat(claims).containsKey(JwtHandler.CLAIM_ROLE_KEY),
                () -> assertThat(claims.get(JwtHandler.CLAIM_ID_KEY)).isEqualTo("1"),
                () -> assertThat(claims.get(JwtHandler.CLAIM_ROLE_KEY)).isEqualTo("MEMBER")
        );
    }

    @Test
    void 잘못된_이메일_비번으로_요청하면_예외를_발생한다() {
        // given
        final LoginRequest loginRequest = new LoginRequest("wrongEmail@gmail.com", "password");

        // when & then
        assertThatThrownBy(() -> loginService.login(loginRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("회원 정보가 존재하지 않습니다.");
    }

}
