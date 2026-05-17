package roomescape.common.auth;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.exception.DomainException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static roomescape.common.exception.GlobalErrorCode.INVALID_GUEST_NAME_HEADER;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String GUEST_NAME_HEADER = "X-Guest-Name";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String authorization = getAuthorizationHeader(webRequest);
        return getName(authorization);
    }

    private static String getAuthorizationHeader(NativeWebRequest webRequest) {
        String authorization = webRequest.getHeader(GUEST_NAME_HEADER);

        if(authorization == null || authorization.isBlank()) {
            throw new DomainException(INVALID_GUEST_NAME_HEADER);
        }
        return authorization;
    }

    private static String getName(String authorization) {
        String decode;
        try {
            decode = URLDecoder.decode(authorization.trim(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DomainException(INVALID_GUEST_NAME_HEADER);
        }
        if(decode.isBlank()) {
            throw new DomainException(INVALID_GUEST_NAME_HEADER);
        }
        return decode;
    }
}
