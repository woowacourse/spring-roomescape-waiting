package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.exception.auth.AuthenticationException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTokenTest {

    @Test
    void 쿠키에서_authToken을_성공적으로_추출한다() {
        // given
        String expectedToken = "sampleAuthToken";
        Cookie[] cookies = {new Cookie("authToken", expectedToken)};
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        // when
        AuthToken authToken = AuthToken.extractFrom(request);

        // then
        assertThat(authToken.value()).isEqualTo(expectedToken);
    }

    @Test
    void 쿠키가_없는_요청에서_AuthenticationException이_발생한다() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> AuthToken.extractFrom(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("로그인해주세요.");
    }

    @Test
    void authToken_쿠키가_없는_경우_AuthenticationException이_발생한다() {
        // given
        Cookie[] cookies = {new Cookie("otherToken", "someValue")};
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        // when, then
        assertThatThrownBy(() -> AuthToken.extractFrom(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("로그인해주세요.");
    }

    @Test
    void 복수의_쿠키에서_authToken을_성공적으로_추출한다() {
        // given
        String expectedToken = "sampleAuthToken";
        Cookie[] cookies = {
                new Cookie("firstToken", "value1"),
                new Cookie("authToken", expectedToken),
                new Cookie("lastToken", "value2")
        };
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        // when
        AuthToken authToken = AuthToken.extractFrom(request);

        // then
        assertThat(authToken.value()).isEqualTo(expectedToken);
    }
}
