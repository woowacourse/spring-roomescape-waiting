package roomescape.config;

import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import roomescape.exception.AuthorizationExpiredException;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.service.MemberAuthService;

public class MemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberAuthService memberAuthService;

    public MemberArgumentResolver(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType()
                .equals(MemberProfileInfo.class);
    }

    @Override
    public MemberProfileInfo resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                             NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Cookie[] cookies = Objects.requireNonNull(request)
                .getCookies();

        if (memberAuthService.isLoginMember(cookies)) {
            return memberAuthService.extractPayload(cookies);
        }
        throw new AuthorizationExpiredException();
    }
}
