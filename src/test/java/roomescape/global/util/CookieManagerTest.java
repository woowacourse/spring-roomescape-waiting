package roomescape.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookieManagerTest {

    @DisplayName("쿠키_배열에서_token_쿠키를_정상적으로_추출한다")
    @Test
    void 쿠키_배열에서_token_쿠키를_정상적으로_추출한다() {
        // given
        Cookie[] cookies = {
                new Cookie("session", "abc123"),
                new Cookie("token", "jwt-token-value"),
                new Cookie("other", "xyz")
        };

        // when
        String token = CookieManager.extractTokenFromCookies(cookies);

        // then
        assertThat(token).isEqualTo("jwt-token-value");
    }

    @DisplayName("token 쿠키가 없으면 빈 문자열을 반환한다")
    @Test
    void token_쿠키가_없으면_빈_문자열을_반환한다() {
        // given
        Cookie[] cookies = {
                new Cookie("session", "abc123"),
                new Cookie("auth", "not-a-token")
        };

        // when
        String token = CookieManager.extractTokenFromCookies(cookies);

        // then
        assertThat(token).isEqualTo(null);
    }
}
