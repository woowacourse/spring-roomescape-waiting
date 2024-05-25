package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.core.dto.auth.TokenResponse;
import roomescape.infrastructure.TokenProvider;

class CookieServiceTest {
    private final CookieService cookieService = new CookieService();

    @Test
    @DisplayName("응답으로 받은 토큰을 담은 쿠키를 생성한다.")
    void createCookie() {
        final TokenProvider tokenProvider = new TokenProvider();
        final String accessToken = tokenProvider.createToken("test", "test");
        final TokenResponse tokenResponse = new TokenResponse(accessToken);

        final Cookie cookie = cookieService.createCookie(tokenResponse);

        assertAll(
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.getValue()).isEqualTo(accessToken)
        );
    }

    @Test
    void createEmptyCookie() {
        final Cookie cookie = cookieService.createEmptyCookie();

        assertAll(
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getMaxAge()).isZero()
        );
    }

    @Test
    void extractCookies() {
        final Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("token", "test");

        final String extractedToken = cookieService.extractCookies(cookies);

        assertThat(extractedToken).isEqualTo("test");
    }
}