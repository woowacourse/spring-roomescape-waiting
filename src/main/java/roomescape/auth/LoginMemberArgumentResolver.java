package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.InsufficientRoleException;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDao memberDao;

    public LoginMemberArgumentResolver(JwtTokenProvider jwtTokenProvider, MemberDao memberDao) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDao = memberDao;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && (Member.class.isAssignableFrom(parameter.getParameterType())
                || Long.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = TokenExtractor.extract(request);
        if (token == null) {
            throw new UnauthorizedException();
        }

        Long memberId = jwtTokenProvider.getMemberId(token);
        LoginMember annotation = parameter.getParameterAnnotation(LoginMember.class);
        Role[] requiredRoles = annotation.role();
        boolean needsMember = Member.class.isAssignableFrom(parameter.getParameterType());
        boolean needsRoleCheck = requiredRoles.length > 0;

        if (needsMember || needsRoleCheck) {
            Member member = memberDao.findById(memberId);

            if (needsRoleCheck && !Arrays.asList(requiredRoles).contains(member.getRole())) {
                throw new InsufficientRoleException();
            }

            if (needsMember) {
                return member;
            }
        }
        return memberId;
    }
}
