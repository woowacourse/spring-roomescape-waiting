package roomescape.auth.web.controller;

import static roomescape.auth.web.constant.AuthConstant.AUTH_COOKIE_KEY;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.application.AuthService;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.controller.dto.LoginRequest;
import roomescape.auth.web.support.AuthorizationExtractor;
import roomescape.global.exception.AuthenticationException;
import roomescape.global.util.CookieUtils;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthorizationExtractor<String> authorizationExtractor;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public void login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String authenticationToken = authService.createAuthenticationToken(loginRequest.email(),
                loginRequest.password());
        response.addCookie(CookieUtils.createBasic(AUTH_COOKIE_KEY, authenticationToken));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/login/check")
    public AuthenticatedMember loginCheck(HttpServletRequest request) {
        String token = authorizationExtractor.extract(request);
        return authService.getAuthenticatedMember(token);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = CookieUtils.findFromCookiesByName(request.getCookies(), AUTH_COOKIE_KEY)
                .orElseThrow(() -> new AuthenticationException("인증을 위한 쿠키가 존재하지 않습니다."));
        response.addCookie(CookieUtils.toExpiredCookie(cookie));
    }
}
