package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.AuthService;
import roomescape.dto.LoginMember;
import roomescape.dto.MemberResponse;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthService authService;

    public LoginMemberArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberConverter.class);
    }

    @Override
    public LoginMember resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED);
        }
        Cookie[] cookies = request.getCookies();
        MemberResponse response = authService.findMemberByCookies(cookies);
        return new LoginMember(response.id(), response.name());
    }

}
