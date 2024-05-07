package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.LoginService;
import roomescape.service.dto.request.TokenRequest;
import roomescape.service.dto.response.AuthenticationInfoResponse;
import roomescape.service.dto.response.TokenResponse;

@RestController
public class LoginController {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final int COOKIE_AGE_SECONDS = 60 * 60 * 5;

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid TokenRequest tokenRequest,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = loginService.login(tokenRequest);

        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, tokenResponse.accessToken())
                .httpOnly(true)
                .maxAge(COOKIE_AGE_SECONDS)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<AuthenticationInfoResponse> loginCheck(HttpServletRequest request) {
        AuthenticationInfoResponse authenticationInfoResponse = loginService.loginCheck(request);
        return ResponseEntity.ok()
                .body(authenticationInfoResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME)
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }
}
