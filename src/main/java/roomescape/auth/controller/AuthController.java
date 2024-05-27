package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.common.util.CookieUtils;
import roomescape.member.dto.MemberResponse;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = authService.createMemberToken(loginRequest);
        CookieUtils.setCookieByToken(response, token);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/member")
    public ResponseEntity<MemberResponse> findMemberNameByLoginMember(LoginMember loginMember) {
        MemberResponse memberResponse = authService.findMemberNameByLoginMember(loginMember);

        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        CookieUtils.clearTokenAndCookie(response);

        return ResponseEntity.ok().build();
    }
}
