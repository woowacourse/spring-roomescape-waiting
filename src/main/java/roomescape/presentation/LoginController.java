package roomescape.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.AuthService;
import roomescape.dto.MemberResponse;
import roomescape.dto.TokenRequest;
import roomescape.dto.TokenResponse;

@RestController
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody TokenRequest tokenRequest, HttpServletResponse response) {
        TokenResponse token = authService.createToken(tokenRequest);
        authService.setToken(response, token.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> check(HttpServletRequest request) {
        String token = authService.extractToken(request.getCookies());
        MemberResponse memberResponse = authService.findMemberByToken(token);
        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        response.addCookie(new Cookie("token", ""));
    }
}
