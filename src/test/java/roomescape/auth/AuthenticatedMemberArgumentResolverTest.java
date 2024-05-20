package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import roomescape.auth.dto.Accessor;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class AuthenticatedMemberArgumentResolverTest {
    @InjectMocks
    private AuthenticatedMemberArgumentResolver resolver;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberService memberService;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest request;

    @DisplayName("지원하지 않는 파라미터인 경우 false 반환")
    @Test
    void supportsParameter_ReturnsFalse_IfParameterDoesNotHaveAnnotation() {
        when(methodParameter.hasParameterAnnotation(Authenticated.class)).thenReturn(false);
        boolean supports = resolver.supportsParameter(methodParameter);

        assertThat(supports).isFalse();
    }


    @DisplayName("정상적으로 Accessor 객체를 반환")
    @Test
    void should_resolve_argument_and_return_accessor() {
        when(webRequest.getNativeRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(
                new Cookie[]{new Cookie("token", "valid-token")});
        when(jwtTokenProvider.getAccessorId("valid-token")).thenReturn(1L);

        Accessor accessor = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(accessor.id()).isEqualTo(1L);
    }
}
