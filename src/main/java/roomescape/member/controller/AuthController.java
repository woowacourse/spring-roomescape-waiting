package roomescape.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.auth.PermitAll;
import roomescape.member.auth.jwt.JwtTokenExtractor;
import roomescape.member.controller.dto.LoginCheckResponse;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.service.AuthService;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;
    private final JwtTokenExtractor jwtTokenExtractor;

    @PermitAll
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "token=" + token + "; Path=/; HttpOnly");
        headers.add("Keep-Alive", "timeout=60");

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "token=; Path=/; HttpOnly; Max-Age=0");

        return ResponseEntity.ok().headers(headers).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(HttpServletRequest request) {
        final String token = jwtTokenExtractor.extractTokenFromCookie(request.getCookies());
        return ResponseEntity.ok(authService.checkLogin(token));
    }

    @PermitAll
    @PostMapping("/signup")
    public ResponseEntity<MemberInfoResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }
}
