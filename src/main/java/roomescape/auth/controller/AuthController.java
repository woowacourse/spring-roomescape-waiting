package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.principal.AuthenticatedMember;
import roomescape.auth.service.AuthService;
import roomescape.resolver.Authenticated;
import roomescape.util.CookieParser;

import java.util.Optional;

@RestController
public class AuthController {

    private static final String AUTHENTICATION_COOKIE_NAME = "token";
    private static final int AUTHENTICATION_COOKIE_MAX_AGE = 60 * 60 * 24;

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public void login(
            @RequestBody final LoginRequest loginRequest,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final String authenticationToken = authService.login(loginRequest);

        deleteCookie(request, response);
        addCookie(
                response,
                AUTHENTICATION_COOKIE_NAME,
                authenticationToken,
                AUTHENTICATION_COOKIE_MAX_AGE
        );
    }

    private void deleteCookie(final HttpServletRequest request, final HttpServletResponse response) {
        final Optional<Cookie> cookie = CookieParser.findCookie(request, AUTHENTICATION_COOKIE_NAME);

        if (cookie.isPresent()) {
            final Cookie cookieValue = cookie.get();
            cookieValue.setValue("");
            cookieValue.setPath("/");
            cookieValue.setMaxAge(0);
            response.addCookie(cookieValue);
        }
    }

    private void addCookie(
            final HttpServletResponse response,
            final String cookieName,
            final String value,
            final int maxAge
    ) {
        final Cookie cookie = new Cookie(cookieName, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    @GetMapping("/login/check")
    public LoginCheckResponse checkLogin(@Authenticated AuthenticatedMember authenticatedMember) {
        return new LoginCheckResponse(authenticatedMember.name());
    }

    @PostMapping("/logout")
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        deleteCookie(request, response);
    }
}
