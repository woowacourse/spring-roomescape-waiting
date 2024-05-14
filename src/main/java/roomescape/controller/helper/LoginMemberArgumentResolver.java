package roomescape.controller.helper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.member.Role;
import roomescape.global.JwtManager;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtManager jwtManager;

    public LoginMemberArgumentResolver(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
        boolean hasMemberType = LoginMember.class.isAssignableFrom(parameter.getParameterType());
        return hasLoginAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = jwtManager.extractToken(request);
        Claims claims = jwtManager.verifyToken(token);
        long memberId = Long.parseLong(claims.getSubject());
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);
        return new LoginMember(memberId, name, Role.getRole(role));
    }
}
