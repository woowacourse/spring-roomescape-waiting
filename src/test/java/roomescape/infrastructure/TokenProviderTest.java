package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.acceptance.AcceptanceTest;

class TokenProviderTest extends AcceptanceTest {

    @Autowired
    TokenProvider tokenProvider;

    @DisplayName("쿠키가 없는 경우를 확인한다.")
    @Test
    void doesNotHasCookie() {
        Cookie[] cookies = {};

        boolean doesNotHasCookie = tokenProvider.doesNotHasCookie(cookies);

        assertThat(doesNotHasCookie).isTrue();
    }

    @DisplayName("쿠키가 있는 경우를 확인한다.")
    @Test
    void hasCookie() {
        Cookie[] cookies = {new Cookie("token", userToken)};

        boolean doesNotHasCookie = tokenProvider.doesNotHasCookie(cookies);

        assertThat(doesNotHasCookie).isFalse();
    }

    @DisplayName("토큰이 없는 경우를 확인한다.")
    @Test
    void doesNotRequestHasToken() {
        Cookie[] cookies = {};

        boolean doesNotHasToken = tokenProvider.doesNotHasToken(cookies);

        assertThat(doesNotHasToken).isTrue();
    }

    @DisplayName("토큰이 있는 경우를 확인한다.")
    @Test
    void hasToken() {
        Cookie[] cookies = {new Cookie("token", userToken)};

        boolean doesNotHasToken = tokenProvider.doesNotHasToken(cookies);

        assertThat(doesNotHasToken).isFalse();
    }
}
