package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.auth.service.AuthService;

@RequestMapping("/login")
@RestController
@RequiredArgsConstructor
public class LoginRestController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(
            @RequestBody final LoginRequest loginRequest,
            final HttpServletResponse response
    ) {

        final String accessToken = authService.createToken(
                loginRequest.email(),
                loginRequest.password()
        );

        final Cookie cookie = new Cookie("token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.ok(new LoginResponse(accessToken));
    }

    @GetMapping("/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(
            final HttpServletRequest request
    ) {

        final Cookie[] cookies = request.getCookies();
        final String token = extractTokenFromCookie(cookies);
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String name = authService.findNameByToken(token);

        return ResponseEntity.ok(new LoginCheckResponse(name));
    }

    private String extractTokenFromCookie(final Cookie[] cookies) {
        if (cookies == null) {
            return "";
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }

        return "";
    }
}
