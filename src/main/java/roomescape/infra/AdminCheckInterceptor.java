package roomescape.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.dto.MemberInfo;
import roomescape.exception.RoomescapeException;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final MemberService memberService;

    public AdminCheckInterceptor(TokenService tokenService, MemberService memberService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        try {
            String token = TokenExtractor.extractFrom(request.getCookies());
            long memberIdFromToken = tokenService.findMemberIdFromToken(token);
            MemberInfo memberInfo = memberService.findByMemberId(memberIdFromToken);
            sendRedirectIfNotAdmin(response, memberInfo);
            return memberInfo.role().equals(Role.ADMIN.name());
        } catch (RoomescapeException e) {
            response.sendRedirect("/");
            return false;
        }
    }

    private void sendRedirectIfNotAdmin(HttpServletResponse response, MemberInfo memberInfo) throws IOException {
        if (!memberInfo.role().equals(Role.ADMIN.name())) {
            response.sendRedirect("/");
        }
    }
}
