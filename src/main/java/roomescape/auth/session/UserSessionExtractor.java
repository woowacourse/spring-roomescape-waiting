package roomescape.auth.session;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.common.cookie.manager.CookieManager;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

@Component
@RequiredArgsConstructor
public class UserSessionExtractor {

    private final JwtManager jwtManager;
    private final CookieManager cookieManager;

    public UserSession execute(final HttpServletRequest request) {

        final Jwt accessToken = Jwt.from(
                cookieManager.extractCookie(request, TokenType.ACCESS.getDescription()));

        final Claims claims = jwtManager.parse(accessToken);

        return new UserSession(
                UserId.from(claims.get(UserSession.Fields.id, Long.class)),
                UserName.from(claims.get(UserSession.Fields.name, String.class)),
                UserRole.valueOf(claims.get(UserSession.Fields.role, String.class))
        );
    }
}
