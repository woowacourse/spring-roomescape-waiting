package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.global.annotation.AuthenticationPrincipal;
import roomescape.member.controller.dto.response.MemberNameResponse;
import roomescape.member.domain.Member;

@RestController
public class TokenAuthController {
    private final AuthService authService;

    public TokenAuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid final LoginRequest loginRequest,
                                      final HttpServletResponse response) {
        Cookie cookie = new Cookie("token", authService.createToken(loginRequest));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberNameResponse> getUserInformation(@AuthenticationPrincipal final Member member) {
        return ResponseEntity.ok()
                .body(new MemberNameResponse(member.getNameValue()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
