package roomescape.member.presentation.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.util.CookieManager;
import roomescape.member.application.service.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.LoginRequest;
import roomescape.member.presentation.dto.MemberResponse;

@RestController
public class LoginLogoutController {

    private final MemberService memberService;

    public LoginLogoutController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public void userLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        String jwtToken = memberService.createToken(loginRequest);
        Cookie cookie = CookieManager.createJwtLoginCookie(jwtToken);
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setStatus(200);
    }

    @GetMapping("/login/check")
    public ResponseEntity<?> checkLogin(Member member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = CookieManager.createJwtLogoutCookie();
        response.addCookie(cookie);
    }
}
