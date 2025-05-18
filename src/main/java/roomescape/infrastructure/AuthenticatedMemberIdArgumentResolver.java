package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.auth.dto.MemberIdDto;

@Component
public class AuthenticatedMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor;

    public AuthenticatedMemberIdArgumentResolver(AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor) {
        this.authenticatedMemberIdExtractor = authenticatedMemberIdExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedMemberId.class);
    }

    @Override
    public MemberIdDto resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        return authenticatedMemberIdExtractor.extract(request);
    }
}
