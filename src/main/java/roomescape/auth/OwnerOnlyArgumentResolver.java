package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.exception.UnauthorizedException;
import roomescape.reservation.exception.ReservationErrorCode;

@Component
public class OwnerOnlyArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasParameterAnnotation = parameter.hasParameterAnnotation(OwnerOnly.class);
        boolean isAssignable = String.class.isAssignableFrom(parameter.getParameterType());
        return hasParameterAnnotation && isAssignable;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String loginName = (String) request.getAttribute("loginName");

        if (loginName == null) {
            throw new UnauthorizedException(ReservationErrorCode.MISSING_AUTH_HEADER.getMessage());
        }

        return loginName;
    }
}
