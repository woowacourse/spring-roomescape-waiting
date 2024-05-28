package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthConstants;
import roomescape.exception.UnauthorizedException;
import roomescape.service.auth.AuthService;
import roomescape.service.auth.dto.LoginCheckResponse;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.auth.dto.SignUpRequest;
import roomescape.service.member.dto.MemberResponse;
import roomescape.util.CookieUtil;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> register(@RequestBody @Valid SignUpRequest signUpRequest) {
        MemberResponse memberResponse = authService.create(signUpRequest);
        return ResponseEntity.created(URI.create("/members/" + memberResponse.id())).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        String token = authService.login(loginRequest).token();
        Cookie cookie = CookieUtil.createCookie(AuthConstants.AUTH_COOKIE_NAME, token);

        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(CookieUtil.expiredCookie(AuthConstants.AUTH_COOKIE_NAME));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public LoginCheckResponse check(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();

        return CookieUtil.searchValueFromKey(cookies, AuthConstants.AUTH_COOKIE_NAME)
                .map(authService::check)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다."));
    }
}
