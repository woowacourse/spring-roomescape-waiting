package roomescape.member.presentation.controller.guest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.application.AuthService;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.dto.response.CheckLoginResponse;
import roomescape.common.security.dto.response.LoginResponse;
import roomescape.common.security.infrastructure.CookieManager;
import roomescape.member.application.MemberApplicationService;
import roomescape.member.presentation.dto.request.SignupWebRequest;
import roomescape.member.presentation.dto.response.SignUpWebResponse;

@RestController
public class GuestMemberController {

    private static final String TOKEN = "token";

    private final MemberApplicationService memberApplicationService;
    private final AuthService authService;
    private final CookieManager cookieManager;

    public GuestMemberController(final MemberApplicationService memberApplicationService, final AuthService authService,
                                 final CookieManager cookieManager) {
        this.memberApplicationService = memberApplicationService;
        this.authService = authService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/members")
    public ResponseEntity<SignUpWebResponse> signUp(final @RequestBody SignupWebRequest signupWebRequest) {
        SignUpWebResponse response = memberApplicationService.signup(signupWebRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(final @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.makeCookie(TOKEN, loginResponse.accessToken()).toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginResponse> checkLogin(final MemberInfo memberInfo) {
        return ResponseEntity.ok(CheckLoginResponse.from(memberApplicationService.getById(memberInfo.id())));
    }
}
