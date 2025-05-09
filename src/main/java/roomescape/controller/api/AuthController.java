package roomescape.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AuthenticationCheckResponse;
import roomescape.service.AuthService;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String accessToken = authService.createToken(loginRequest);
        Cookie cookie = new Cookie("token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/check")
    public ResponseEntity<AuthenticationCheckResponse> authenticationCheck(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        try {
            return ResponseEntity
                    .ok(AuthenticationCheckResponse.from(authService.checkAuthenticationStatus(cookies)));
        } catch (NullPointerException exception) {
            return ResponseEntity
//                    .ok(AuthenticationCheckResponse.from(new Member(null, "익명", "a@a.com", "a")));
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }
}
