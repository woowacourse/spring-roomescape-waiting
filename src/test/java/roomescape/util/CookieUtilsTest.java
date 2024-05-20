package roomescape.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilsTest {

    @DisplayName("쿠키에서 TOKEN_KEY로 토큰을 추출한다.")
    @Test
    void extractTokenFromCookie() {
        String tokenKeyValue = "jwt";
        Cookie[] cookies = {new Cookie(CookieUtils.TOKEN_KEY, tokenKeyValue)};

        String token = CookieUtils.extractTokenFromCookie(cookies);

        assertThat(token).isEqualTo(tokenKeyValue);
    }

    @DisplayName("쿠키에서 유효하지 않은 키로 토큰을 추출하면 null을 반환한다.")
    @Test
    void extractTokenFromCookieWithInvalidKey() {
        Cookie[] cookies = {new Cookie("invalidKey", "jwt")};

        String token = CookieUtils.extractTokenFromCookie(cookies);

        assertThat(token).isNull();
    }

    @DisplayName("쿠키를 설정해 응답에 포함한다.")
    @Test
    void setCookieByToken() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "jwt";
        CookieUtils.setCookieByToken(response, token);

        Cookie cookie = response.getCookie(CookieUtils.TOKEN_KEY);

        assertAll(
                () -> assertThat(cookie.getValue()).isEqualTo(token),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(CookieUtils.COOKIE_MAX_AGE),
                () -> assertThat(cookie.getPath()).isEqualTo(CookieUtils.PATH)
        );
    }

    @DisplayName("토큰과 쿠키를 제거한다.")
    @Test
    void clearTokenAndCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.clearTokenAndCookie(response);

        Cookie cookie = response.getCookie(CookieUtils.TOKEN_KEY);

        assertAll(
                () -> assertThat(cookie.getValue()).isEqualTo(null),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(0),
                () -> assertThat(cookie.getPath()).isEqualTo(CookieUtils.PATH)
        );
    }
}
