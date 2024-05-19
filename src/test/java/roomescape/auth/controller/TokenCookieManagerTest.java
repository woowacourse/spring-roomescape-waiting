package roomescape.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.auth.exception.NotLoginAuthenticationException;

class TokenCookieManagerTest {
    private final TokenCookieManager tokenCookieManager = new TokenCookieManager();

    @DisplayName("토큰이 쿠키에 있을 경우, 토큰 값을 반환한다.")
    @Test
    void getTokenTest_whenTokenExist() {
        String token = "abc";
        Cookie[] cookies = new Cookie[]{
                new Cookie("token", token), new Cookie("notThing", "exist")};

        String actual = tokenCookieManager.getToken(cookies);

        assertThat(actual).isEqualTo(token);
    }

    @DisplayName("토큰이 쿠키에 없을 경우, 예외를 발생시킨다.")
    @Test
    void getTokenTest_whenTokenNotExist() {
        Cookie[] cookies = new Cookie[]{
                new Cookie("not", "thing"), new Cookie("notThing", "exist")};

        assertThatThrownBy(() -> tokenCookieManager.getToken(cookies))
                .isInstanceOf(NotLoginAuthenticationException.class);
    }

    @DisplayName("쿠키가 null인 경우, 예외를 발생시킨다.")
    @Test
    void getTokenTest_whenTokenIsNull() {
        Cookie[] cookies = null;

        assertThatThrownBy(() -> tokenCookieManager.getToken(cookies))
                .isInstanceOf(NotLoginAuthenticationException.class);
    }

    @DisplayName("토큰을 통해 쿠키를 만들 수 있다.")
    @Test
    void createResponseCookieTest() {
        String token = "abc";

        ResponseCookie actual = tokenCookieManager.createResponseCookie(token);

        assertAll(
                () -> assertThat(actual.getName()).isEqualTo("token"),
                () -> assertThat(actual.getValue()).isEqualTo(token));
    }
}
