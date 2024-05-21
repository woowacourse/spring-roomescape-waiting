package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.controller.request.LoginRequest;
import roomescape.controller.response.LoginResponse;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;
import roomescape.service.dto.MemberInfo;

@Controller
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    public AuthController(AuthService authService, CookieUtils cookieUtils) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthDto authDto = AuthDto.from(loginRequest);
        String token = authService.createToken(authDto);
        ResponseCookie cookie = cookieUtils.createCookie(token, 3600);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginResponse> checkLogin(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = cookieUtils.extractToken(cookies);
        MemberInfo loginMember = authService.checkToken(token);
        LoginResponse response = LoginResponse.from(loginMember);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = cookieUtils.createCookie(null, 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
