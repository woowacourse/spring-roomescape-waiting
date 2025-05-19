package roomescape.auth.cookie.extractor;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.common.cookie.extractor.CookieExtractor;
import roomescape.common.cookie.extractor.MissingCookieException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CookieExtractorImplTest {

    @Autowired
    private CookieExtractor cookieExtractor;

    @Test
    @DisplayName("쿠키가 존재할 경우, 해당 쿠키 값을 반환한다")
    void shouldReturnCookieValueWhenCookieExists() {
        // given
        final String targetCookieName = "sessionId";
        final String targetCookieValue = "abc123";
        final List<Cookie> cookies = List.of(
                new Cookie("user", "testUser"),
                new Cookie(targetCookieName, targetCookieValue),
                new Cookie("lang", "en"));

        // when
        final String result = cookieExtractor.execute(cookies, targetCookieName);

        // then
        assertThat(result).isEqualTo(targetCookieValue);
    }

    @Test
    @DisplayName("쿠키가 존재하지 않을 경우, MissingCookieException 예외를 던진다")
    void shouldThrowMissingCookieExceptionWhenCookieDoesNotExist() {
        // given
        final String targetCookieName = "missingCookie";
        final List<Cookie> cookies = List.of(
                new Cookie("user", "testUser"),
                new Cookie("sessionId", "abc123"),
                new Cookie("lang", "en"));

        // when
        // then
        assertThatThrownBy(() -> cookieExtractor.execute(cookies, targetCookieName))
                .isInstanceOf(MissingCookieException.class)
                .hasMessage(targetCookieName);
    }

    @Test
    @DisplayName("쿠키 리스트가 비어있을 경우, MissingCookieException 예외를 던진다")
    void shouldThrowMissingCookieExceptionWhenCookiesListIsEmpty() {
        // given
        final String targetCookieName = "testCookie";
        final List<Cookie> cookies = List.of();

        // when
        // then
        assertThatThrownBy(() -> cookieExtractor.execute(cookies, targetCookieName))
                .isInstanceOf(MissingCookieException.class)
                .hasMessage(targetCookieName);
    }
}
