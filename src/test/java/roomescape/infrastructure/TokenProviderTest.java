package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.acceptance.AcceptanceTest;

class TokenProviderTest extends AcceptanceTest {

    @Autowired
    TokenProvider tokenProvider;

    @Test
    void doesNotHasCookie() {
        Cookie[] cookies = {};

        boolean doesNotHasCookie = tokenProvider.doesNotHasCookie(cookies);

        assertThat(doesNotHasCookie).isTrue();
    }

    @Test
    void hasCookie() {
        Cookie[] cookies = {new Cookie("token", userToken)};

        boolean doesNotHasCookie = tokenProvider.doesNotHasCookie(cookies);

        assertThat(doesNotHasCookie).isFalse();
    }

    @Test
    void doesNotRequestHasToken() {
        Cookie[] cookies = {};

        boolean doesNotHasToken = tokenProvider.doesNotHasToken(cookies);

        assertThat(doesNotHasToken).isTrue();
    }

    @Test
    void hasToken() {
        Cookie[] cookies = {new Cookie("token", userToken)};

        boolean doesNotHasToken = tokenProvider.doesNotHasToken(cookies);

        assertThat(doesNotHasToken).isFalse();
    }
}
