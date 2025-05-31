package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AuthenticationException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationTokenHandler tokenHandler;

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 토큰을 발급받을 수 있다.")
    void issueToken() {
        // given
        var email = "admin@email.com";
        var password = "password";

        // when
        var token = service.issueToken(email, password);

        // then
        assertThat(tokenHandler.isValidToken(token)).isTrue();
        var userId = tokenHandler.extractId(token);
        var user = userRepository.findById(userId).orElseThrow();
        assertThat(user.email()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 토큰 발급 시 예외가 발생한다.")
    void issueToken_WhenEmailNotFound() {
        // given
        var wrongEmail = "wrong@email.com";
        var password = "password";

        // when & then
        assertThatThrownBy(() -> service.issueToken(wrongEmail, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("이메일이 틀렸습니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 토큰 발급 시 예외가 발생한다.")
    void issueToken_WhenPasswordWrong() {
        // given
        var email = "admin@email.com";
        var wrongPassword = "wrongpassword";

        // when & then
        assertThatThrownBy(() -> service.issueToken(email, wrongPassword))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("토큰으로 사용자 정보를 조회할 수 있다.")
    void getUserByToken() {
        // given
        var email = "admin@email.com";
        var password = "password";
        var token = service.issueToken(email, password);

        // when
        User user = service.getUserByToken(token);

        // then
        assertThat(user.email()).isEqualTo(email);
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 사용자 조회 시 예외가 발생한다.")
    void getUserByToken_WhenTokenInvalid() {
        // given
        var invalidToken = "invalid-token";

        // when & then
        assertThatThrownBy(() -> service.getUserByToken(invalidToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("토큰이 만료되었거나 유효하지 않습니다.");
    }
}
