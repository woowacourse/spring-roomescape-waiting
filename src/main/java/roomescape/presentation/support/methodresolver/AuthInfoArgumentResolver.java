package roomescape.presentation.support.methodresolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.infrastructure.error.exception.JwtExtractException;
import roomescape.infrastructure.error.exception.UnauthorizedException;
import roomescape.infrastructure.security.AccessToken;
import roomescape.infrastructure.security.JwtProvider;
import roomescape.presentation.support.JwtTokenExtractor;

@Component
public class AuthInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public AuthInfoArgumentResolver(JwtTokenExtractor jwtTokenExtractor,
                                    JwtProvider jwtProvider,
                                    MemberRepository memberRepository) {
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthInfo.class)
                && parameter.hasParameterAnnotation(AuthPrincipal.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Member member = getMemberFromRequest(request);
        return new AuthInfo(member.getId(), member.getName(), member.getRole());
    }

    private Member getMemberFromRequest(HttpServletRequest request) {
        Long identifier = getIdentifier(request);
        return memberRepository.findById(identifier)
                .orElseThrow(() -> new UnauthorizedException("접근 권한이 없습니다."));
    }

    private Long getIdentifier(HttpServletRequest request) {
        try {
            AccessToken accessToken = jwtTokenExtractor.extract(request);
            return jwtProvider.extractIdentifier(accessToken);
        } catch (JwtExtractException e) {
            throw new UnauthorizedException("인증 정보를 확인할 수 없습니다.", e);
        }
    }
}
