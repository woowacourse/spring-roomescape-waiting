package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.exception.AuthenticationException;
import roomescape.infrastructure.JwtTokenHandler;

@Import({AuthenticationService.class, JwtTokenHandler.class})
class AuthenticationServiceTest extends ServiceTest {

    @Autowired
    private AuthenticationService service;

    private final String email = "razel@email.com";
    private final String password = "password";

    @BeforeEach
    void setUp() {
        repositoryHelper.saveUser(new User(
            new UserName("라젤"),
            new Email(email),
            new Password(password)
        ));
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 토큰을 발행한다.")
    void issueToken() {
        assertThatCode(() -> service.issueToken(email, password))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰 발행 시 이메일로 유저를 못 찾으면 예외가 발생한다.")
    void issueTokenWithWrongEmail() {
        assertThatThrownBy(() -> service.issueToken("xxxx@email.com", password))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("토큰 발행 시 비밀번호가 틀리면 예외가 발생한다.")
    void issueTokenWithWrongPassword() {
        assertThatThrownBy(() -> service.issueToken(email, "xxxx"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("토큰으로 유저를 찾을 수 있다.")
    void getUserByToken() {
        // given
        var issuedToken = service.issueToken(email, password);

        // when
        User foundUser = service.getUserByToken(issuedToken);

        // then
        assertThat(foundUser.email()).isEqualTo(new Email(email));
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 예외가 발생한다.")
    void findUserByInvalidToken() {
        // given
        var issuedToken = service.issueToken(email, password);
        var invalidToken = issuedToken.substring(0, issuedToken.length() - 1);

        // when & then
        assertThatThrownBy(() -> service.getUserByToken(invalidToken))
                .isInstanceOf(AuthenticationException.class);
    }
}
