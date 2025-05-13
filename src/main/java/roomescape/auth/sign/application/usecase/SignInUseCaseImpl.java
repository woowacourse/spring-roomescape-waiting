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
public class SignInUseCaseImpl implements SignInUseCase {

    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtManager jwtManager;

    @Override
    public SignInResult execute(final SignInRequest request) {
        final User user = userQueryService.getByEmail(request.email());
        final Password saved = user.getPassword();
        validatePassword(saved, request.rawPassword(), user.getEmail());

        final Jwt accessToken = jwtManager.generate(buildClaims(user), TokenType.ACCESS);
        return SignInResult.from(buildCookie(accessToken));
    }

    private void validatePassword(final Password saved,
                                  final String rawPassword,
                                  final Email email) {
        if (saved.matches(rawPassword, passwordEncoder)) {
            return;
        }
        throw new InvalidSignInException(email);
    }

    private Claims buildClaims(final User user) {
        return Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();
    }

    private Cookie buildCookie(final Jwt accessToken) {
        final Cookie cookie = new Cookie(TokenType.ACCESS.getDescription(), accessToken.getValue());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(TokenType.ACCESS.getPeriodInSeconds());
        return cookie;
    }
}
