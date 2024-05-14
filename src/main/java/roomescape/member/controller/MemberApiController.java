package roomescape.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.CookieUtils;
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.service.MemberService;

@RestController
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberSignUpRequest memberSignUpRequest) {
        Long memberId = memberService.save(memberSignUpRequest);

        return ResponseEntity.created(URI.create("/members/" + memberId)).build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAll() {
        List<MemberResponse> memberResponses = memberService.findAll();

        return ResponseEntity.ok(memberResponses);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                      HttpServletResponse response) {
        String token = memberService.createMemberToken(memberLoginRequest);
        CookieUtils.setCookieBy(response, token);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> loginCheck(@Login LoginMemberInToken loginMemberInToken) {
        MemberResponse memberResponse = memberService.findMemberNameByLoginMember(loginMemberInToken);

        return ResponseEntity.ok(memberResponse);
    }
}
