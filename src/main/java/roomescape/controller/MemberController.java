package roomescape.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.annotation.Auth;
import roomescape.dto.LoginRequest;
import roomescape.dto.UserInfo;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final TokenService tokenService;

    public MemberController(MemberService memberService, TokenService tokenService) {
        this.memberService = memberService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        long userId = memberService.login(loginRequest);
        LocalDateTime now = LocalDateTime.now();
        Duration tokenLifeTime = Duration.between(now, now.plusHours(1));
        String token = tokenService.createToken(userId, now, tokenLifeTime);
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .maxAge(tokenLifeTime)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<UserInfo> myInfo(@Auth long userId) {
        UserInfo userInfo = memberService.findByUserId(userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/members")
    public List<UserInfo> allMembers() {
        return memberService.findAll();
    }
}
