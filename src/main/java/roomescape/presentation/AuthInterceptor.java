package roomescape.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.AuthService;
import roomescape.application.AuthorizationExtractor;
import roomescape.application.MemberService;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Role;
import roomescape.exception.AccessDeniedException;
import roomescape.exception.UnauthorizedException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final MemberService memberService;
    private final AuthorizationExtractor<String> authorizationExtractor;

    public AuthInterceptor(
            AuthService authService,
            MemberService memberService,
            AuthorizationExtractor<String> authorizationExtractor
    ) {
        this.authService = authService;
        this.memberService = memberService;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String token = authorizationExtractor.extract(request)
                .orElseThrow(UnauthorizedException::new);

        Long memberId = authService.getMemberIdByToken(token);
        MemberResponse memberResponse = memberService.getById(memberId);

        if (memberResponse.role() != Role.ADMIN) {
            throw new AccessDeniedException("어드민 권한이 필요합니다.");
        }

        return true;
    }
}
