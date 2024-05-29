package roomescape.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.auth.domain.Token;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.facade.AuthFacadeService;
import roomescape.auth.provider.CookieProvider;
import roomescape.member.dto.MemberLoginCheckResponse;

@RestController
public class AuthController {

    private final AuthFacadeService authService;

    public AuthController(AuthFacadeService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        Token token = authService.login(loginRequest);
        ResponseCookie responseCookie = CookieProvider.setCookieFrom(token);

        return ResponseEntity.ok()
                .header("Set-Cookie", responseCookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public MemberLoginCheckResponse loginCheck(@LoginMemberId Long id) {
        return authService.findLoginMemberInfo(id);
    }
}
