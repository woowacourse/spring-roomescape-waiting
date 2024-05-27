package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.Authenticated;
import roomescape.service.auth.AuthService;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.response.MemberResponse;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest loginRequest) {
        ResponseCookie cookie = authService.login(loginRequest);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> checkLogin(@Authenticated AuthInfo authInfo) {
        MemberResponse memberResponse = MemberResponse.from(authInfo);
        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = authService.logout();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
