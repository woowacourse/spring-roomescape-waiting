package roomescape.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Role;
import roomescape.dto.login.LoginMember;
import roomescape.dto.token.TokenDto;
import roomescape.infrastructure.AuthorizationExtractor;
import roomescape.service.AuthService;

@Component
public class UserAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthorizationExtractor authorizationExtractor;
    private final AuthService authService;

    public UserAuthorizationInterceptor(AuthorizationExtractor authorizationExtractor, AuthService authService) {
        this.authorizationExtractor = authorizationExtractor;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = authorizationExtractor.extractToken(request);
        TokenDto tokenDto = new TokenDto(token);

        if (!authService.isValidateToken(tokenDto)) {
            return AuthorizationResponseHandler.redirectLogin(response);
        }

        LoginMember loginMember = authService.extractLoginMemberByToken(tokenDto);
        if (loginMember.role() != Role.USER) {
            return AuthorizationResponseHandler.responseUnauthorized(response);
        }

        request.setAttribute("loginMember", loginMember);
        return true;
    }
}
