package roomescape.auth.session.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.auth.session.Session;
import roomescape.common.cookie.manager.CookieManager;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

@Component
@RequiredArgsConstructor
public class UserSessionExtractor {

    private final JwtManager jwtManager;
    private final CookieManager cookieManager;

    public Session execute(final HttpServletRequest request) {

        final Jwt accessToken = Jwt.from(
                cookieManager.extractCookie(request, TokenType.ACCESS.getDescription()));

        final Claims claims = jwtManager.parse(accessToken);

        return new Session(
                UserId.from(claims.get(Session.Fields.id, Long.class)),
                UserName.from(claims.get(Session.Fields.name, String.class)),
                UserRole.valueOf(claims.get(Session.Fields.role, String.class))
        );
    }
}
