package roomescape.auth.sign.application.usecase;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.sign.application.dto.SignInResult;

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

