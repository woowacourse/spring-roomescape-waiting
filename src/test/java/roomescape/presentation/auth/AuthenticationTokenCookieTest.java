package roomescape.presentation.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthenticationTokenCookieTest {

    @Test
    @DisplayName("인증 토큰의 존재 여부를 알 수 있다.")
    void hasToken() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthenticationTokenCookie.COOKIE_KEY, "abcd"));

        var tokenCookie = AuthenticationTokenCookie.fromRequest(request);

        // when
        boolean hasToken = tokenCookie.hasToken();

        // then
        assertThat(hasToken).isTrue();
    }

    @Test
    @DisplayName("HTTP 요청에서 인증 토큰을 추출한다.")
    void fromRequest() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthenticationTokenCookie.COOKIE_KEY, "abcd"));

        // when
        var tokenCookie = AuthenticationTokenCookie.fromRequest(request);

        // then
        assertAll(
                () -> assertThat(tokenCookie).isNotNull(),
                () -> assertThat(Objects.requireNonNull(tokenCookie).token()).isEqualTo("abcd")
        );
    }

    @Test
    @DisplayName("HTTP 응답을 위한 인증 토큰 쿠키를 생성한다.")
    void forResponse() {
        var cookie = AuthenticationTokenCookie.forResponse("abcd");

        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(AuthenticationTokenCookie.COOKIE_KEY),
                () -> assertThat(cookie.getValue()).isEqualTo("abcd"),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.getPath()).isEqualTo("/")
        );
    }

    @Test
    @DisplayName("인증 토큰을 즉시 만료시키기 위한 인증 토큰 쿠키를 생성한다.")
    void forExpire() {
        var cookie = AuthenticationTokenCookie.forExpire();

        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(AuthenticationTokenCookie.COOKIE_KEY),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(0)
        );
    }
}
