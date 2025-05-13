package roomescape.member.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.service.AuthService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String TOKEN = "token";
    private final AuthService authService;

    public LoginMemberArgumentResolver(AuthService authService) {
        this.authService = authService;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(MemberResponse.class);
    }

    @Override
    public Object resolveArgument
            (MethodParameter parameter,
             ModelAndViewContainer mavContainer,
             NativeWebRequest webRequest,
             WebDataBinderFactory binderFactory
            )
            throws Exception
    {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthenticatedException("[ERROR] 로그인이 필요합니다.");
        }

        String token = extractTokenFromCookie(cookies);

        return authService.findUserByToken(token);
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (TOKEN.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
