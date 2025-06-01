package roomescape.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.jwt.CookieManager;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.RegistrationRequest;
import roomescape.member.service.AuthService;

@RestController
public class AuthController {

    private final AuthService loginService;

    public AuthController(AuthService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/members")
    public ResponseEntity<Void> registerMember(@Valid @RequestBody RegistrationRequest registrationRequest) {
        loginService.signup(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> processLogin(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {
        String token = loginService.createToken(loginRequest);
        Cookie cookie = CookieManager.setCookie("token", token);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> checkLogin(Member member) {
        return ResponseEntity.ok().body(MemberResponse.from(member));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> processLogout(HttpServletResponse response) {
        Cookie cookie = CookieManager.expireCookie("name");
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}
