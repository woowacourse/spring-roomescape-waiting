package roomescape.common.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.exception.MissingTokenExcpetion;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_COOKIE_NAME = "token";

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Member.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory) throws Exception {

        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final String token = extractTokenFromCookies(request.getCookies());

        if (token == null || token.isBlank()) {
            throw new MissingTokenExcpetion("Token is missing");
        }

        final String email = jwtTokenProvider.getPayload(token);
        return memberService.findMemberByEmail(email);
    }

    private String extractTokenFromCookies(final Cookie[] cookies) {
        if (cookies == null) {
            throw new MissingTokenExcpetion("Token is missing");
        }

        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new MissingTokenExcpetion("Token is missing");
    }
}
