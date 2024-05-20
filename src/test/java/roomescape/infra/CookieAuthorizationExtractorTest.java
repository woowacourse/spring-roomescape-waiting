package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookieAuthorizationExtractorTest {

    @Mock
    private HttpServletRequest request;

    private CookieAuthorizationExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new CookieAuthorizationExtractor();
    }

    @Test
    @DisplayName("쿠키들이 없을 경우 빈 값을 반환한다.")
    void extractCookieWhenCookiesAreEmpty() {
        given(request.getCookies()).willReturn(null);

        Optional<String> result = extractor.extract(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("'token' 쿠키에서 토큰을 추출한다.")
    void extract() {
        Cookie tokenCookie = new Cookie("token", "token_value");
        Cookie[] cookies = {tokenCookie};

        given(request.getCookies()).willReturn(cookies);

        Optional<String> result = extractor.extract(request);

        assertThat(result).isPresent().contains("token_value");
    }

    @Test
    @DisplayName("'token' 쿠키가 없으면 빈 값을 반환한다.")
    void extractWhenTokenCookieDoesNotExist() {
        Cookie[] cookies = {new Cookie("another_cookie", "another_cookie_value")};

        given(request.getCookies()).willReturn(cookies);

        Optional<String> result = extractor.extract(request);

        assertThat(result).isEmpty();
    }
}
