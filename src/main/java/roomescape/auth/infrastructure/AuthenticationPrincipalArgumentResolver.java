package roomescape.auth.infrastructure;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.service.AuthService;
import roomescape.error.NotFoundException;
import roomescape.error.UnauthorizedException;
import roomescape.member.domain.Member;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        try {
            final Member member = authService.extractMemberByRequest(request);
            return new LoginMember(
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getRole());
        } catch (IllegalArgumentException | NotFoundException e) {
            log.error(e.getMessage());
            throw new UnauthorizedException("인증에 실패했습니다.");
        }
    }
}
