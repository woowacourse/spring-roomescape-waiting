package roomescape.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.local.NotFoundCookieException;

class CookieUtilityTest {

    private final CookieUtility cookieUtility = new CookieUtility();

    @Nested
    @DisplayName("요청에서 원하는 쿠키를 찾을 수 있다.")
    public class findCookie {

        @DisplayName("원하는 쿠키를 찾을 수 있다.")
        @Test
        void canGetCookie() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie[] cookies = {
                    new Cookie("target_key", "target_value"),
                    new Cookie("key1", "value1"),
                    new Cookie("key2", "value2")};
            doReturn(cookies).when(request).getCookies();

            // when
            Cookie cookie = cookieUtility.getCookie(request, "target_key");

            // then
            assertAll(
                    () -> assertThat(cookie.getName()).isEqualTo("target_key"),
                    () -> assertThat(cookie.getValue()).isEqualTo("target_value")
            );
        }

        @DisplayName("요청에 쿠키가 아예 존재하지 않을 경우 예외를 발생시킨다.")
        @Test
        void cannotGetCookieByEmptyCookieInRequest() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            doReturn(null).when(request).getCookies();

            // when & then
            assertThatThrownBy(() -> cookieUtility.getCookie(request, "target_key"))
                    .isInstanceOf(NotFoundCookieException.class)
                    .hasMessage("쿠키가 존재하지 않습니다.");
        }

        @DisplayName("요구하는 쿠키가 없는 경우 예외를 발생시킨다.")
        @Test
        void cannotGetCookieByWantedCookieInRequest() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie[] cookies = {
                    new Cookie("key1", "value1"),
                    new Cookie("key2", "value2")};
            doReturn(cookies).when(request).getCookies();

            // when & then
            assertThatThrownBy(() -> cookieUtility.getCookie(request, "target_key"))
                    .isInstanceOf(NotFoundCookieException.class)
                    .hasMessage("쿠키가 존재하지 않습니다.");
        }
    }
}
