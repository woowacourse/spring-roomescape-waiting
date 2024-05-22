package roomescape.member.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.dto.SignUpRequest;
import roomescape.member.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberIdNameResponse> findMembers() {
        return memberService.findMembers();
    }

    @PostMapping
    public ResponseEntity<MemberNameResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        String token = memberService.signUp(signUpRequest);

        ResponseCookie responseCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new MemberNameResponse(signUpRequest.name()));
    }
}
