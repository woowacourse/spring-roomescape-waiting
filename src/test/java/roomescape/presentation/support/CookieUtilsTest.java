package roomescape.presentation.support;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilsTest {

    private final CookieUtils cookieUtils = new CookieUtils();

    @Test
    @DisplayName("응답의 쿠키에 토큰을 세팅한다.")
    void test1() {
        // given
        final String token = "token";
        final String cookieNameForToken = "token";
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        cookieUtils.setCookieForToken(response, token);

        // then
        Optional<Cookie> foundCookie = Arrays.stream(response.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieNameForToken))
                .findAny();

        assertAll(
                () -> assertThat(foundCookie).isPresent(),
                () -> assertThat(foundCookie.get().getValue()).isEqualTo(token)
        );
    }

    @Test
    @DisplayName("토큰을 가져온다")
    void test2() {
        // given
        final String token = "token";
        final String cookieNameForToken = "token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(cookieNameForToken, token));

        // when
        String parsedToken = cookieUtils.getToken(request);

        // then
        assertThat(parsedToken).isEqualTo(token);
    }
}
