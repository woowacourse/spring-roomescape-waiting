package roomescape.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.exception.UnauthorizedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CookieProviderTest {

    private final CookieProvider cookieProvider = new CookieProvider();

    @Test
    void 쿠키를_생성할_수_있다() {
        //given
        String token = "test-token";

        //when & then
        assertThat(cookieProvider.create(token)).isInstanceOf(ResponseCookie.class);
    }

    @Test
    void 쿠키를_무효화할_수_있다() {
        //given
        ResponseCookie cookie = cookieProvider.invalidate(new Cookie("token", "test-token"));

        //when
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(0);
    }

    @Test
    void 쿠키_배열에서_토큰을_추출할_수_있다() {
        //given
        Cookie[] cookies = {
                new Cookie("token", "test-token"),
                new Cookie("token2", "test-token2")
        };

        //when
        String extractedToken = cookieProvider.extractTokenFromCookies(cookies);

        //then
        assertThat(extractedToken).isEqualTo("test-token");
    }

    @Test
    void 쿠키에서_토큰을_추출할_수_있다() {
        //given
        Cookie cookie = new Cookie("token", "test-token");

        //when
        String extractedToken = cookieProvider.extractTokenFromCookie(cookie);

        //then
        assertThat(extractedToken).isEqualTo("test-token");
    }

    @Test
    void 쿠키가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> cookieProvider.extractTokenFromCookies(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 토큰_쿠키가_없으면_예외가_발생한다() {
        //given
        Cookie[] cookies = {
                new Cookie("token11", "abcd"),
                new Cookie("token22", "efgh")
        };

        //when & then
        assertThatThrownBy(() -> cookieProvider.extractTokenFromCookies(cookies))
                .isInstanceOf(UnauthorizedException.class);
    }
}
