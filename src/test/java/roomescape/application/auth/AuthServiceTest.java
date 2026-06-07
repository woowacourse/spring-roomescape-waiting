package roomescape.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.security.Pbkdf2PasswordEncoder;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.user.UserRole;
import roomescape.presentation.auth.request.LoginRequest;
import roomescape.presentation.auth.request.SignupRequest;

@DisplayName("인증 서비스")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final Pbkdf2PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Test
    @DisplayName("비밀번호가 일치하면 로그인할 수 있다")
    void login() {
        // given
        String encodedPassword = passwordEncoder.encode("password");
        User user = User.of(1L, "홍길동", encodedPassword, UserRole.USER);
        given(userRepository.findByName("홍길동")).willReturn(Optional.of(user));

        AuthService authService = new AuthService(userRepository, passwordEncoder);

        // when
        User loginUser = authService.login(new LoginRequest("홍길동", "password"));

        // then
        assertThat(loginUser.getId()).isEqualTo(1L);
        assertThat(loginUser.getName()).isEqualTo("홍길동");
        assertThat(loginUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("사용자가 없으면 로그인할 수 없다")
    void loginWhenUserNotFound() {
        // given
        given(userRepository.findByName("홍길동")).willReturn(Optional.empty());
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("홍길동", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("비밀번호가 다르면 로그인할 수 없다")
    void loginWhenPasswordMismatch() {
        // given
        String encodedPassword = passwordEncoder.encode("password");
        User user = User.of(1L, "홍길동", encodedPassword, UserRole.USER);
        given(userRepository.findByName("홍길동")).willReturn(Optional.of(user));
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("홍길동", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("회원가입하면 사용자를 생성한다")
    void signup() {
        // given
        given(userRepository.existsByName("새사용자")).willReturn(false);
        User savedUser = User.of(2L, "새사용자", passwordEncoder.encode("password"), UserRole.USER);
        given(userRepository.save(any())).willReturn(savedUser);
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        // when
        User signupUser = authService.signup(new SignupRequest("새사용자", "password"));

        // then
        assertThat(signupUser.getId()).isEqualTo(2L);
        assertThat(signupUser.getName()).isEqualTo("새사용자");
        assertThat(signupUser.getRole()).isEqualTo(UserRole.USER);
        verify(userRepository).existsByName("새사용자");
    }

    @Test
    @DisplayName("이미 존재하는 사용자는 회원가입할 수 없다")
    void signupWhenUserAlreadyExists() {
        // given
        given(userRepository.existsByName("홍길동")).willReturn(true);
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        // when & then
        assertThatThrownBy(() -> authService.signup(new SignupRequest("홍길동", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
        verify(userRepository).existsByName("홍길동");
    }
}
