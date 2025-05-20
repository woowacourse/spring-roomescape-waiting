package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.RequireRole;
import roomescape.auth.dto.CheckLoginResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.auth.dto.MemberInfo;
import roomescape.auth.infrastructure.CookieManager;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.MemberRole;
import roomescape.member.service.MemberService;

@RestController
public class AuthController {

    private static final String TOKEN = "token";

    private final AuthService authService;
    private final MemberService memberService;
    private final CookieManager cookieManager;

    public AuthController(final AuthService authService, final MemberService memberService,
                          final CookieManager cookieManager) {
        this.authService = authService;
        this.memberService = memberService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(final @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.makeCookie(TOKEN, loginResponse.accessToken()).toString())
                .build();
    }

    @RequireRole(MemberRole.USER)
    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginResponse> checkLogin(final MemberInfo memberInfo) {
        return ResponseEntity.ok(CheckLoginResponse.from(memberService.getMember(memberInfo)));
    }

    @RequireRole(MemberRole.USER)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse httpServletResponse) {
        cookieManager.deleteCookie(httpServletResponse, TOKEN);
        return ResponseEntity.noContent().build();
    }
}
