package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.JwtProperties;
import roomescape.auth.service.AuthService;
import roomescape.exception.UnauthorizedException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String COOKIE_TOKEN = "token";

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public void login(@RequestBody @Valid final LoginRequest request, final HttpServletResponse response) {
        log.debug("로그인 시작");
        final String token = authService.createToken(request);
        log.debug("토큰 생성 완료");

        final ResponseCookie cookie = ResponseCookie.from(COOKIE_TOKEN, token)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtProperties.getExpireLength())
                .build();
        log.debug("쿠키 생성 완료");

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("로그인 성공");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(final HttpServletResponse response) {
        final ResponseCookie cookie = ResponseCookie.from(COOKIE_TOKEN, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @GetMapping("/login/check")
    public LoginCheckResponse checkLogin(@CookieValue(name = COOKIE_TOKEN, required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("토큰 쿠키가 존재하지 않습니다.");
        }
        return authService.checkLogin(token);
    }
}
