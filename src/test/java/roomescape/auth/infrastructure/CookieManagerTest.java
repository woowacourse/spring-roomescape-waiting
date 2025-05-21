package roomescape.auth.infrastructure;

import java.time.Duration;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.auth.config.CookieProperties;

class CookieManagerTest {

    private static final String COOKIE_NAME = "testCookie";
    private static final String COOKIE_VALUE = "testValue";
    private static final String DOMAIN = "localhost";
    private static final long MAX_AGE = 3600L;

    private CookieManager cookieManager;

    @BeforeEach
    void setUp() {
        CookieProperties cookieProperties = new CookieProperties();
        cookieProperties.setDomain(DOMAIN);
        cookieProperties.setMaxAge(MAX_AGE);
        cookieManager = new CookieManager(cookieProperties);
    }

    @Test
    @DisplayName("쿠키를 생성할 때 모든 보안 설정이 올바르게 적용된다.")
    void makeCookie_WithSecuritySettings() {
        // given & when
        ResponseCookie cookie = cookieManager.makeCookie(COOKIE_NAME, COOKIE_VALUE);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
                    softAssertions.assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
                    softAssertions.assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE);
                    softAssertions.assertThat(cookie.getSameSite()).isEqualTo("Strict");
                    softAssertions.assertThat(cookie.getPath()).isEqualTo("/");
                    softAssertions.assertThat(cookie.getDomain()).isEqualTo(DOMAIN);
                    softAssertions.assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(MAX_AGE));
                }
        );
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
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(setCookieHeader).contains("Max-Age=0");
            softAssertions.assertThat(setCookieHeader).contains("HttpOnly");
            softAssertions.assertThat(setCookieHeader).contains("SameSite=Strict");
        });
    }
} 
