package roomescape.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;

@RestController
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = memberService.login(loginRequest);
        Cookie cookie = CookieUtil.create(token);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberPreviewResponse> loginCheck(Member member) {
        MemberPreviewResponse name = MemberPreviewResponse.from(member);
        return ResponseEntity.ok().body(name);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = CookieUtil.expired();
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}
