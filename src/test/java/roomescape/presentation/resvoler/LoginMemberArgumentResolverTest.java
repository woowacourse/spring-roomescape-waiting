package roomescape.presentation.resvoler;

import jakarta.servlet.http.Cookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dto.LoginMember;
import roomescape.model.Member;
import roomescape.model.Role;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@ExtendWith(MockitoExtension.class)
class LoginMemberArgumentResolverTest {

    @Mock
    AuthService authService;

    @Mock
    CookieUtils cookieUtils;

    LoginMemberArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new LoginMemberArgumentResolver(authService, cookieUtils);
    }

    @Test
    void 로그인된_멤버를_성공적으로_리졸브한다() throws Exception {
        // given
        String token = "fakeToken";
        String email = "example@example.com";

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new Cookie("token", token));
        NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

        Member member = new Member(1L, "히로", email, "password", Role.ADMIN);
        given(cookieUtils.getToken(servletRequest)).willReturn(token);
        given(authService.getAuthenticatedMember(token)).willReturn(member);

        // when
        Object result = resolver.resolveArgument(
                mock(MethodParameter.class),
                mock(ModelAndViewContainer.class),
                webRequest,
                mock(WebDataBinderFactory.class)
        );
        LoginMember loginMember = (LoginMember) result;

        // then
        assertAll(
                () -> assertThat(result).isInstanceOf(LoginMember.class),
                () -> assertThat(loginMember.email()).isEqualTo(email)
        );
    }
}
