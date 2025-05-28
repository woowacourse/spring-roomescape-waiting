package roomescape.global.auth.controller;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.domain.dto.TokenRequestDto;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.global.auth.service.AuthService;
import roomescape.user.domain.dto.UserResponseDto;

@RestController
public class AuthController {

    private static final String TOKEN_NAME_FIELD = "token";
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<Void> login(@RequestBody TokenRequestDto tokenRequestDto) {
        TokenResponseDto tokenResponseDto = authService.login(tokenRequestDto);
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, tokenResponseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<UserResponseDto> checkAuth(@CookieValue(name = TOKEN_NAME_FIELD) String token) {
        UserResponseDto userResponseDto = authService.findMemberByToken(token);
        return ResponseEntity.ok().body(userResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = TOKEN_NAME_FIELD) String token) {
        authService.findMemberByToken(token);

        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, "")
                .domain("localhost")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.ofDays(0))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
