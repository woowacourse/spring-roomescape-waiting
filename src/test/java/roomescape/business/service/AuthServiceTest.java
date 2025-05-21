package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.auth.AuthToken;
import roomescape.auth.jwt.JwtUtil;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.exception.auth.AuthenticationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private Users users;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService sut;

    @Test
    void 올바른_이메일과_비밀번호로_인증에_성공한다() {
        // given
        String email = "test@example.com";
        String password = "password123";
        User user = new User("Test User", email, password);
        AuthToken expectedAuth = mock(AuthToken.class);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.createToken(user)).thenReturn(expectedAuth);

        // when
        AuthToken result = sut.authenticate(email, password);

        // then
        assertThat(result).isEqualTo(expectedAuth);
        verify(users).findByEmail(email);
        verify(jwtUtil).createToken(user);
    }

    @Test
    void 존재하지_않는_이메일로_인증_시_예외가_발생한다() {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";

        when(users.findByEmail(email)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.authenticate(email, password))
                .isInstanceOf(AuthenticationException.class);

        verify(users).findByEmail(email);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void 잘못된_비밀번호로_인증_시_예외가_발생한다() {
        // given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        String correctPassword = "correctPassword";
        String encodedPassword = new BCryptPasswordEncoder().encode(correctPassword);
        User user = new User("Test User", email, encodedPassword);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> sut.authenticate(email, wrongPassword))
                .isInstanceOf(AuthenticationException.class);

        verify(users).findByEmail(email);
        verifyNoInteractions(jwtUtil);
    }
}
