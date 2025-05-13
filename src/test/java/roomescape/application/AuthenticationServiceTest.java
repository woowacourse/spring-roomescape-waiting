package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.AuthenticationTokenProvider;
import roomescape.domain.User;
import roomescape.domain.UserRole;
import roomescape.domain.repository.UserRepository;
import roomescape.infrastructure.fake.DummyTokenProvider;
import roomescape.infrastructure.fake.UserFakeRepository;

class AuthenticationServiceTest {

    private final AuthenticationTokenProvider tokenProvider = new DummyTokenProvider();
    private final UserRepository userRepository = new UserFakeRepository();
    private final AuthenticationService service = new AuthenticationService(tokenProvider, userRepository);

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 토큰을 발행할 수 있다.")
    void issueToken() {
        // given
        var email = "popo@email.com";
        var password = "password";
        var user = new User(1L, "포포", UserRole.USER, email, password);
        userRepository.save(user);

        // when & then
        assertThatCode(() -> service.issueToken(email, password))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰 발행 시 이메일로 유저를 못 찾으면 예외가 발생한다.")
    void issueTokenWithWrongEmail() {
        // given
        var user = new User(1L, "포포", UserRole.USER, "popo@email.com", "password");
        userRepository.save(user);

        // when & then
        var wrongEmail = "xxxx@email.com";
        var password = "password";
        assertThatThrownBy(() -> service.issueToken(wrongEmail, password))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("토큰 발행 시 비밀번호가 틀리면 예외가 발생한다.")
    void issueTokenWithWrongPassword() {
        // given
        var user = new User(1L, "포포", UserRole.USER, "popo@email.com", "password");
        userRepository.save(user);

        // when & then
        var email = "poopo@email.com";
        var wrongPassword = "xxxx";
        assertThatThrownBy(() -> service.issueToken(email, wrongPassword))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("토큰으로 유저를 찾을 수 있다.")
    void getUserByToken() {
        // given
        var user = new User(1L, "포포", UserRole.USER, "popo@email.com", "password");
        userRepository.save(new User(1L, "포포", UserRole.USER, "popo@email.com", "password"));

        var issuedToken = service.issueToken(user.email(), user.password());

        // when
        User foundUser = service.getUserByToken(issuedToken);

        // then
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 예외가 발생한다.")
    void findUserByInvalidToken() {
        // given
        var user = new User(1L, "포포", UserRole.USER, "popo@email.com", "password");
        userRepository.save(user);

        var mockTokenProvider = Mockito.mock(AuthenticationTokenProvider.class);
        Mockito.when(mockTokenProvider.createToken(any())).thenReturn("token");
        Mockito.when(mockTokenProvider.isValidToken(anyString())).thenReturn(false);
        var service = new AuthenticationService(mockTokenProvider, userRepository);

        var issuedToken = service.issueToken(user.email(), user.password());

        // when & then
        assertThatThrownBy(() -> service.getUserByToken(issuedToken))
            .isInstanceOf(AuthorizationException.class);
    }
}
