package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthorizationExtractorTest {

    @Test
    void 요청_쿠키에서_토큰을_추출() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("token", "expectedToken")};
        when(request.getCookies()).thenReturn(cookies);

        AuthorizationExtractor extractor = new AuthorizationExtractor();

        //when
        String token = extractor.extractToken(request);

        //then
        assertThat(token).isEqualTo("expectedToken");
    }

}
