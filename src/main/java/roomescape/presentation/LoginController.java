package roomescape.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.AuthService;
import roomescape.config.LoginMemberConverter;
import roomescape.dto.CheckMemberResponse;
import roomescape.dto.LoginMember;
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
    public ResponseEntity<CheckMemberResponse> check(@LoginMemberConverter LoginMember loginMember) {
        return ResponseEntity.ok(new CheckMemberResponse(loginMember.name()));
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        response.addCookie(new Cookie("token", ""));
    }
}
