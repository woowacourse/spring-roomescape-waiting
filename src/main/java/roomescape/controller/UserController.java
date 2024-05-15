package roomescape.controller;

import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.UserLoginRequest;
import roomescape.controller.response.UserNameResponse;
import roomescape.model.Member;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

@RestController
public class UserController {

    private final MemberService memberService;
    private final AuthService authService;

    public UserController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        Member member = memberService.findUserByEmailAndPassword(request);
        Cookie cookie = authService.createCookieByUser(member);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<UserNameResponse> login(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(new UserNameResponse(member.getName()));
    }

    @GetMapping("/members")
    public ResponseEntity<List<Member>> getAllUsers() {
        List<Member> members = memberService.findAllUsers();
        return ResponseEntity.ok(members);
    }
}
