package roomescape.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class CookieManagerTest {

    private static final String COOKIE_NAME = "testCookie";
    private static final String COOKIE_VALUE = "testValue";
    private static final String DOMAIN = "localhost";
    private static final long MAX_AGE = 3600L;

    private CookieManager cookieManager;

    @BeforeEach
    void setUp() {
        cookieManager = new CookieManager();
        ReflectionTestUtils.setField(cookieManager, "domain", DOMAIN);
        ReflectionTestUtils.setField(cookieManager, "maxAge", MAX_AGE);
    }

    @Test
    @DisplayName("쿠키를 생성할 때 모든 보안 설정이 올바르게 적용된다.")
    void makeCookie_WithSecuritySettings() {
        // when
        ResponseCookie cookie = cookieManager.makeCookie(COOKIE_NAME, COOKIE_VALUE);

        // then
        assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE);
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(DOMAIN);
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(MAX_AGE));
    }

    @Test
    @DisplayName("쿠키를 삭제할 때 만료 시간이 0으로 설정된다.")
    void deleteCookie_SetsMaxAgeToZero() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        cookieManager.deleteCookie(response, COOKIE_NAME);

        // then
        String setCookieHeader = response.getHeader("Set-Cookie");
        assertThat(setCookieHeader).contains("Max-Age=0");
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("SameSite=Strict");
    }
} 
