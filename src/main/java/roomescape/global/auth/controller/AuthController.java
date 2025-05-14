package roomescape.global.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.global.auth.dto.CheckLoginResponse;
import roomescape.global.auth.dto.LoginRequest;
import roomescape.global.auth.dto.LoginResponse;
import roomescape.global.auth.dto.UserInfo;
import roomescape.global.auth.infrastructure.CookieManager;
import roomescape.global.auth.service.AuthService;
import roomescape.member.domain.MemberRole;

@RestController
public class AuthController {

    private static final String TOKEN = "token";

    private final AuthService authService;
    private final CookieManager cookieManager;

    public AuthController(final AuthService authService, final CookieManager cookieManager) {
        this.authService = authService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(final @RequestBody LoginRequest loginRequest,
                                      final HttpServletResponse httpServletResponse) {
        LoginResponse loginResponse = authService.createToken(loginRequest);
        cookieManager.addCookieToResponse(httpServletResponse, TOKEN, loginResponse.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginResponse> checkLogin(final UserInfo userInfo) {
        return ResponseEntity.ok(CheckLoginResponse.from(userInfo));
    }

    @RequireRole(MemberRole.USER)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse httpServletResponse) {
        cookieManager.deleteCookie(httpServletResponse, TOKEN);
        return ResponseEntity.noContent().build();
    }
}
