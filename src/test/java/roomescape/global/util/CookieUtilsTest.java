package roomescape.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.fixture.TokenFixture;
import roomescape.global.exception.IllegalRequestException;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @DisplayName("요청으로부터 토큰을 추출할 수 있다")
    @Test
    void should_extract_token_from_request() {
        Cookie[] cookies = {new Cookie("token", "token-value")};
        when(request.getCookies()).thenReturn(cookies);

        assertThat(CookieUtils.getToken(request)).isEqualTo("token-value");
    }

    @DisplayName("요청에서 토큰 추출 시 쿠키가 존재하지 않는 경우 예외가 발생한다")
    @Test
    void should_throw_exception_when_cookies_not_exist() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> CookieUtils.getToken(request))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("요청에서 토큰 추출 시 토큰을 담은 쿠키가 존재하지 않는 경우 예외가 발생한다")
    @Test
    void should_throw_exception_when_token_cookie_not_exist() {
        Cookie[] cookies = {new Cookie("no-token", "dummy-value")};
        when(request.getCookies()).thenReturn(cookies);

        assertThatThrownBy(() -> CookieUtils.getToken(request))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("토큰이 주어졌을 때 토큰의 내용을 담은 쿠키를 생성할 수 있다")
    @Test
    void should_create_cookie_when_requested() {
        Cookie createdCookie = CookieUtils.createTokenCookie(TokenFixture.DUMMY_TOKEN);
        assertAll(
                () -> assertThat(createdCookie.getName()).isEqualTo("token"),
                () -> assertThat(createdCookie.getPath()).isEqualTo("/"),
                () -> assertThat(createdCookie.isHttpOnly()).isTrue()
        );
    }

    @DisplayName("쿠키를 만료시킬 수 있다")
    @Test
    void should_expire_existing_cookie() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        CookieUtils.clearTokenCookie(mockResponse);
        Cookie cookie = mockResponse.getCookie("token");

        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }
}
