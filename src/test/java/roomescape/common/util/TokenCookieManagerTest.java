package roomescape.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.common.exception.LoginException;

class TokenCookieManagerTest {

    private TokenCookieManager cookieManager = new TokenCookieManager();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("정상적으로 토큰을 쿠키에 저장한다.")
    void addTokenCookie_test() {
        // given
        String token = "a";
        // when
        cookieManager.addTokenCookie(response, token);
        // then
        Cookie cookie = response.getCookies()[0];
        assertAll(
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.getName()).isEqualTo("token"),
                () -> assertThat(cookie.getValue()).isEqualTo("a"),
                () -> assertThat(cookie.getPath()).isEqualTo("/")
        );
    }

    @Test
    @DisplayName("정상적으로 토큰가 담긴 쿠키를 삭제한다.")
    void deleteTokenCookie_test() {
        // when
        cookieManager.deleteTokenCookie(response);
        // then
        Cookie cookie = response.getCookies()[0];
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo("token"),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("토큰을 추출하려고 할 때 쿠키가 없으면 예외가 발생한다.")
    void extractTokenFromCookie_exception() {
        assertThatThrownBy(() -> cookieManager.extractTokenFromCookie(request))
                .isInstanceOf(LoginException.class)
                .hasMessage("로그인 되어있지 않습니다.");
    }

    @Test
    @DisplayName("토큰을 쿠키로부터 정상적으로 추출한다.")
    void extractTokenFromCookie_test() {
        // given
        String token = "Aasdsad";
        Cookie otherCookie = new Cookie("token", token);
        request.setCookies(otherCookie);

        // when
        String findToken = cookieManager.extractTokenFromCookie(request);

        // then
        assertThat(findToken).isEqualTo(token);
    }
}