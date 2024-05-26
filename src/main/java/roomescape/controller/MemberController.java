package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.MemberLoginRequest;
import roomescape.controller.request.RegisterRequest;
import roomescape.controller.response.MemberNameResponse;
import roomescape.controller.response.MemberResponse;
import roomescape.model.Member;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

import java.net.URI;
import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    public MemberController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody MemberLoginRequest request, HttpServletResponse response) {
        Member member = memberService.findMemberByEmailAndPassword(request);
        Cookie cookie = authService.createCookieByMember(member);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberNameResponse> login(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(new MemberNameResponse(member.getName()));
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<Member> members = memberService.findAllMembers();
        List<MemberResponse> responses = members.stream()
                .map(MemberResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(authService.expireCookie(request.getCookies()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> registerMember(@RequestBody RegisterRequest registerRequest) {
        Member member = memberService.register(registerRequest);
        MemberResponse response = new MemberResponse(member);
        return ResponseEntity.created(URI.create("/members/" + member.getId())).body(response);
    }
}
