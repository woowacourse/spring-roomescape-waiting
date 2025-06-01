package roomescape.member.presentation.controller.api;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.dto.LoginMemberInfo;
import roomescape.member.application.dto.MemberLoginCommand;
import roomescape.member.application.service.AuthService;
import roomescape.member.presentation.dto.LoginMemberCheckResponse;
import roomescape.member.security.CookieExtractor;

@RestController
@RequestMapping("/auth")
public class AuthApiController {

    private static final int LOGIN_COOKIE_MAX_AGE = 3600;
    private static final int LOGOUT_COOKIE_MAX_AGE = 0;

    private final AuthService authService;
    private final CookieExtractor cookieExtractor;

    public AuthApiController(final AuthService authService, final CookieExtractor cookieExtractor) {
        this.authService = authService;
        this.cookieExtractor = cookieExtractor;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody final MemberLoginCommand request) {
        final String token = authService.tokenLogin(request);
        final ResponseCookie cookie = cookieExtractor.createCookie(token, LOGIN_COOKIE_MAX_AGE);
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginMemberCheckResponse> getLoginMember(final LoginMemberInfo loginMemberInfo) {
        return ResponseEntity.ok(new LoginMemberCheckResponse(loginMemberInfo.name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        final ResponseCookie expiredCookie = cookieExtractor.createCookie("", LOGOUT_COOKIE_MAX_AGE);
        return ResponseEntity.ok()
                .header("Set-Cookie", expiredCookie.toString())
                .build();
    }
}
