package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.domain.MemberRole;

@ExtendWith(MockitoExtension.class)
class CheckAdminInterceptorTest {

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Mock
    private CookieProvider cookieProvider;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private CheckAdminInterceptor checkAdminInterceptor;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void Role이_ADMIN이면_true를_리턴한다() throws Exception {
        //given
        Cookie[] cookies = {new Cookie("token", "admin_cookie")};
        request.setCookies(cookies);
        String token = "admin_cookie";

        //when
        when(cookieProvider.extractTokenFromCookies(cookies)).thenReturn(token);
        when(jwtTokenProvider.extractMemberRoleFromToken(token)).thenReturn(MemberRole.ADMIN);
        boolean result = checkAdminInterceptor.preHandle(request, response, null);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void Role이_ADMIN이_아니면_false를_리턴한다() throws Exception {
        //given
        Cookie[] cookies = {new Cookie("token", "not_admin_cookie")};
        request.setCookies(cookies);
        String token = "not_admin_cookie";

        //when
        when(cookieProvider.extractTokenFromCookies(cookies)).thenReturn(token);
        when(jwtTokenProvider.extractMemberRoleFromToken(token)).thenReturn(MemberRole.USER);

        //then
        assertThat(checkAdminInterceptor.preHandle(request, response, null)).isFalse();
    }
}
