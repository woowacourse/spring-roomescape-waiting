package roomescape.presentation.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import roomescape.exception.AuthenticationInformationNotFoundException;

class AuthInformationExtractorTest {

    @Test
    @DisplayName("쿠키에 토큰 값이 없는 경우, 예외를 발생한다.")
    void cookieWithoutTokenTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThatCode(() -> AuthInformationExtractor.extractToken(request))
                .isInstanceOf(AuthenticationInformationNotFoundException.class);
    }

    @Test
    @DisplayName("요청의 쿠키에서 토큰값을 추출한다.")
    void extractTokenFromRequestTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "valid-token"));
        String actual = AuthInformationExtractor.extractToken(request);
        assertThat(actual).isEqualTo("valid-token");
    }
}
