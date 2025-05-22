package roomescape.presentation.support.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.auth.dto.JwtPayload;
import roomescape.application.support.exception.ForbiddenException;
import roomescape.application.support.exception.JwtExtractException;
import roomescape.application.support.exception.UnauthorizedException;
import roomescape.domain.member.Role;
import roomescape.infrastructure.security.JwtProvider;
import roomescape.presentation.support.JwtTokenExtractor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtProvider jwtProvider;

    public AdminInterceptor(JwtTokenExtractor jwtTokenExtractor, JwtProvider jwtProvider) {
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        JwtPayload jwtPayload = getJwtPayload(request);
        if (jwtPayload.role() != Role.ADMIN) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        return true;
    }

    private JwtPayload getJwtPayload(HttpServletRequest request) {
        try {
            String jwtToken = jwtTokenExtractor.extract(request);
            return jwtProvider.extractPayload(jwtToken);
        } catch (JwtExtractException e) {
            throw new UnauthorizedException("인증 정보를 확인할 수 없습니다.", e);
        }
    }
}
