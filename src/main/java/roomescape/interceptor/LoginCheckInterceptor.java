package roomescape.interceptor;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.token.TokenProvider;
import roomescape.exception.UnauthorizedException;
import roomescape.util.CookieParser;

public class LoginCheckInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    public LoginCheckInterceptor(final TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        final String authenticationToken = parseAuthenticationToken(request);
        validateToken(authenticationToken);

        return true;
    }

    private String parseAuthenticationToken(final HttpServletRequest request) {
        final String accessToken = CookieParser.findCookie(request, "token")
                .orElseThrow(() -> new UnauthorizedException("인증되지 않은 요청입니다."))
                .getValue();

        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException("인증되지 않은 요청입니다.");
        }

        return accessToken;
    }

    private void validateToken(final String authenticationToken) {
        try {
            tokenProvider.getTokenClaims(authenticationToken);
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException("유효하지 않은 인증 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("만료된 인증 토큰입니다.");
        }
    }
}
