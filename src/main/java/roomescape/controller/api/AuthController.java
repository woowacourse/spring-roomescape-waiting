package roomescape.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.dto.member.MemberNameResponseDto;
import roomescape.service.AuthService;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String token = authService.publishLoginToken(loginRequestDto);
        Cookie cookie = createCookie(token);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    private Cookie createCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberNameResponseDto> checkLogin(@CurrentMember LoginInfo loginMember) {
        MemberNameResponseDto memberResponse = new MemberNameResponseDto(loginMember.name());
        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = createCookie(null);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}
