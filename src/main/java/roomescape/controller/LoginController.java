package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.CookieExtractor;
import roomescape.controller.helper.LoginMember;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.Member;
import roomescape.service.LoginService;
import roomescape.service.dto.LoginCheckResponse;
import roomescape.service.dto.LoginRequest;
import roomescape.service.dto.SignupRequest;
import roomescape.service.dto.SignupResponse;

@RestController
public class LoginController {
    private final LoginService loginService;
    private final CookieExtractor cookieExtractor;

    public LoginController(LoginService loginService, CookieExtractor cookieExtractor) {
        this.loginService = loginService;
        this.cookieExtractor = cookieExtractor;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        String token = loginService.login(request);
        Cookie cookie = cookieExtractor.createCookie(token);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @RoleAllowed
    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> loginCheck(@LoginMember Member member) {
        LoginCheckResponse response = loginService.loginCheck(member);
        return ResponseEntity.ok().body(response);
    }

    @RoleAllowed
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = cookieExtractor.deleteCookie();
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = loginService.signup(request);
        return ResponseEntity.created(URI.create("/members/" + response.getId())).body(response);
    }
}
