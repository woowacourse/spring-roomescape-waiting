package roomescape.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseCookie;
import roomescape.exception.AuthenticationException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenCookieManagerTest {

    @Autowired
    private TokenCookieManager tokenCookieManager;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("토큰 값을 이용하여 쿠키를 생성한다.")
    @Test
    void createTokenCookie() {
        // given
        String token = "token";

        // when
        ResponseCookie tokenCookie = tokenCookieManager.createTokenCookie(token);

        // then
        assertThat(tokenCookie).isNotNull();
        assertThat(tokenCookie.isHttpOnly()).isTrue();
        assertThat(tokenCookie.getPath()).isEqualTo("/");
    }

    @DisplayName("쿠키에서 토큰을 만료시킨다.")
    @Test
    void expireTokenCookie() {
        // when
        ResponseCookie expired = tokenCookieManager.expireTokenCookie();

        // then
        assertThat(expired.getValue()).isEmpty();
    }

    @DisplayName("쿠키가 Null이거나 존재하지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void extractTokenWithEmptyCookies(Cookie[] cookies) {
        // when & then
        assertThatThrownBy(() -> tokenCookieManager.extractTokenBy(cookies))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("쿠키 정보가 존재하지 않습니다.");
    }

    @DisplayName("쿠키에 토큰 정보가 존재하지 않으면 예외가 발생한다.")
    @Test
    void extractTokenWithCookiesHasNotTokenInfo() {
        // when
        Cookie[] cookies = new Cookie[]{new Cookie("name", "value")};

        // then
        assertThatThrownBy(() -> tokenCookieManager.extractTokenBy(cookies))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("쿠키에 토큰 정보가 존재하지 않습니다.");
    }
}
