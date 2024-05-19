package roomescape.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilsTest {

    @DisplayName("HTTP 응답에 쿠키를 추가한다.")
    @Test
    void add_cookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.addCookie(response, "token", "value", 300);

        assertThat(response.getCookie("token").getValue()).isEqualTo("value");
    }

    @DisplayName("HTTP 요청으로부터 쿠키를 가져온다.")
    @Test
    void find_cookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("token", "choco");
        request.setCookies(cookie);

        Optional<Cookie> token = CookieUtils.findCookie(request, "token");

        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token.get().getValue()).isEqualTo("choco")
        );
    }

    @DisplayName("HTTP 쿠키를 만료시킨다.")
    @Test
    void delete_cookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.deleteCookie(response, "token");

        assertThat(response.getCookie("token").getValue()).isNull();
    }
}
