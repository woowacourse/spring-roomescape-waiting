package roomescape.auth.cookie.setter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.cookie.setter.CookieSetter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class CookieSetterImplTest {

    @Autowired
    private CookieSetter cookieSetter;

    @Test
    @DisplayName("쿠키가 제공되면 HttpServletResponse에 추가된다")
    void givenCookie_whenExecute_thenCookieIsAddedToResponse() {
        // given
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final Cookie cookie = new Cookie("testCookie", "testValue");

        // when
        cookieSetter.execute(response, cookie);

        // then
        verify(response, times(1)).addCookie(cookie);
    }

    @Test
    @DisplayName("null 쿠키가 제공되면 예외가 발생한다")
    void givenNullCookie_whenExecute_thenThrowsIllegalArgumentException() {
        // given
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        // then
        assertThatThrownBy(() -> cookieSetter.execute(response, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cookie cannot be null");
    }

    @Test
    @DisplayName("null HttpServletResponse가 제공되면 예외가 발생한다")
    void givenNullResponse_whenExecute_thenThrowsIllegalArgumentException() {
        // given
        final Cookie cookie = new Cookie("testCookie", "testValue");

        // when
        // then
        assertThatThrownBy(() -> cookieSetter.execute(null, cookie))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("HTTP response cannot be null");
    }
}
