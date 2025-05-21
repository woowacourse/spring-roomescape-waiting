package roomescape.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

class AuthorizationExtractorTest {

    private final AuthorizationExtractor authorizationExtractor = new AuthorizationExtractor();

    @Test
    @DisplayName("유효한 쿠키에서 토큰을 추출한다.")
    void extract_WithValidCookie_ReturnsToken() {
        // given
        String token = "valid.token.here";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "token=" + token);

        // when
        String extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("토큰이 없는 쿠키에서 null을 반환한다.")
    void extract_WithNoToken_ReturnsNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "other=value");

        // when
        String extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).isNull();
    }

    @Test
    @DisplayName("세미콜론이 포함된 쿠키에서 토큰을 추출한다.")
    void extract_WithCookieContainingSemicolon_ReturnsToken() {
        // given
        String token = "valid.token.here";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "token=" + token + "; other=value");

        // when
        String extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("NativeWebRequest에서 토큰을 추출한다.")
    void extract_WithNativeWebRequest_ReturnsToken() {
        // given
        String token = "valid.token.here";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "token=" + token);
        NativeWebRequest webRequest = new ServletWebRequest(request);

        // when
        String extractedToken = authorizationExtractor.extract(webRequest);

        // then
        assertThat(extractedToken).isEqualTo(token);
    }


    @Test
    @DisplayName("토큰이 없는 쿠키에서 null을 반환한다.")
    void extract_WithNativeWebRequestWithNoToken_ReturnsNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "other=value");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        // when
        String extractedToken = authorizationExtractor.extract(webRequest);

        // then
        assertThat(extractedToken).isNull();
    }
} 
