package roomescape.auth.sign.application.usecase;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.auth.session.UserSession;
import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.application.dto.SignInResult;
import roomescape.auth.sign.exception.InvalidSignInException;
import roomescape.auth.sign.password.Password;
import roomescape.auth.sign.password.PasswordEncoder;
import roomescape.common.domain.Email;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignOutUseCase {

    public SignInResult execute() {
        // TODO DELETE REFRESH TOKEN
        return SignInResult.from(buildCookie());
    }

    private Cookie buildCookie() {
        final Cookie cookie = new Cookie(TokenType.ACCESS.getDescription(), TokenType.DELETE.getDescription());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(TokenType.DELETE.getPeriodInSeconds());
        return cookie;
    }
}

