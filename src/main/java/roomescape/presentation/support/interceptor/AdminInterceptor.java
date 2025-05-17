package roomescape.presentation.support.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.infrastructure.error.exception.ForbiddenException;
import roomescape.infrastructure.error.exception.JwtExtractException;
import roomescape.infrastructure.error.exception.UnauthorizedException;
import roomescape.infrastructure.security.AccessToken;
import roomescape.infrastructure.security.JwtProvider;
import roomescape.presentation.support.JwtTokenExtractor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public AdminInterceptor(JwtTokenExtractor jwtTokenExtractor,
                            JwtProvider jwtProvider,
                            MemberRepository memberRepository) {
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        Member member = getMemberFromRequest(request);
        if (member.isAdmin()) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        return true;
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
