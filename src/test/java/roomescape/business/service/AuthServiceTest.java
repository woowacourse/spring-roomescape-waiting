package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.auth.AuthToken;
import roomescape.auth.jwt.JwtUtil;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.UserRepository;
import roomescape.exception.auth.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService sut;

    @Test
    void 올바른_이메일과_비밀번호로_인증에_성공한다() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(password);
        User user = User.restore("user-id", "USER", "Test User", email, encodedPassword);
        AuthToken expectedAuth = mock(AuthToken.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.createToken(user)).thenReturn(expectedAuth);

        // when
        AuthToken result = sut.authenticate(email, password);

        // then
        assertThat(result).isEqualTo(expectedAuth);
        verify(userRepository).findByEmail(email);
        verify(jwtUtil).createToken(user);
    }

    @Test
    void 존재하지_않는_이메일로_인증_시_예외가_발생한다() {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.authenticate(email, password))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void 잘못된_비밀번호로_인증_시_예외가_발생한다() {
        // given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        String correctPassword = "correctPassword";
        String encodedPassword = new BCryptPasswordEncoder().encode(correctPassword);
        User user = User.restore("user-id", "USER", "Test User", email, encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> sut.authenticate(email, wrongPassword))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(jwtUtil);
    }
}
