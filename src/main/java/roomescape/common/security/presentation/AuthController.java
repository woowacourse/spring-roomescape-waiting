package roomescape.common.security.presentation;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.response.CheckLoginResponse;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.common.security.dto.response.LoginResponse;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.infrastructure.CookieManager;
import roomescape.common.security.application.AuthService;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.service.MemberDomainService;

@RestController
public class AuthController {

    private static final String TOKEN = "token";

    private final AuthService authService;
    private final MemberDomainService memberDomainService;
    private final CookieManager cookieManager;

    public AuthController(final AuthService authService, final MemberDomainService memberDomainService,
                          final CookieManager cookieManager) {
        this.authService = authService;
        this.memberDomainService = memberDomainService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(final @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.makeCookie(TOKEN, loginResponse.accessToken()).toString())
                .build();
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginResponse> checkLogin(final MemberInfo memberInfo) {
        return ResponseEntity.ok(CheckLoginResponse.from(memberDomainService.getMember(memberInfo.id())));
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse httpServletResponse) {
        cookieManager.deleteCookie(httpServletResponse, TOKEN);
        return ResponseEntity.noContent().build();
    }
}
