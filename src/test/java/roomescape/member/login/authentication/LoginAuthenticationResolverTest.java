package roomescape.member.login.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;

class LoginAuthenticationResolverTest {

    @DisplayName("Argument Resolver를 사용해 토큰을 통한 회원 조회 기능을 구현한다")
    @Test
    void resolveArgument() {
        String token = "valid-token";
        MemberResponse expectedMember = new MemberResponse(1L, "name", "email");

        TokenAuthorizationHandler authorizationHandler = mock(TokenAuthorizationHandler.class);
        MemberService memberService = mock(MemberService.class);

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        MethodParameter methodParameter = mock(MethodParameter.class);

        when(webRequest.getNativeRequest()).thenReturn(httpRequest);
        when(authorizationHandler.extractToken(httpRequest)).thenReturn(token);
        when(memberService.findByToken(token)).thenReturn(expectedMember);
        when(methodParameter.hasParameterAnnotation(AuthenticationPrincipal.class)).thenReturn(true);

        LoginAuthenticationResolver resolver = new LoginAuthenticationResolver(memberService, authorizationHandler);
        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isEqualTo(expectedMember);
    }
}
