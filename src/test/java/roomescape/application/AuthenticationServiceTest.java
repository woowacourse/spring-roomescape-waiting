package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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

    @BeforeEach
    void setUp() {
        var user = User.createUser("라젤", "razel@email.com", "password");
        userRepository.save(user);
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 토큰을 발행할 수 있다.")
    void issueToken() {
        // given
        var email = "razel@email.com";
        var password = "password";

        // when & then
        assertThatCode(() -> service.issueToken(email, password))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰 발행 시 이메일로 유저를 못 찾으면 예외가 발생한다.")
    void issueTokenWithWrongEmail() {
        // when & then
        var wrongEmail = "xxxx@email.com";
        var password = "password";
        assertThatThrownBy(() -> service.issueToken(wrongEmail, password))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("토큰 발행 시 비밀번호가 틀리면 예외가 발생한다.")
    void issueTokenWithWrongPassword() {
        // when & then
        var email = "poopo@email.com";
        var wrongPassword = "xxxx";
        assertThatThrownBy(() -> service.issueToken(email, wrongPassword))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("토큰으로 유저를 찾을 수 있다.")
    void getUserByToken() {
        // given
        var email = "razel@email.com";
        var password = "password";
        var issuedToken = service.issueToken(email, password);

        // when
        User foundUser = service.getUserByToken(issuedToken);

        // then
        assertThat(foundUser.email()).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 예외가 발생한다.")
    void findUserByInvalidToken() {
        // given
        var email = "razel@email.com";
        var password = "password";
        var issuedToken = service.issueToken(email, password);
        var invalidToken = issuedToken.substring(0, issuedToken.length() - 1);

        // when & then
        assertThatThrownBy(() -> service.getUserByToken(invalidToken))
                .isInstanceOf(AuthenticationException.class);
    }
}
