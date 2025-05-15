package roomescape.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.service.MemberService;

@Controller
@RequestMapping("/signup")
public class SignupApiController {
    private final MemberService memberService;

    public SignupApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> signup(@ModelAttribute MemberSignupRequest memberSignupRequest) {
        MemberResponse memberResponse = memberService.add(memberSignupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponse);
    }
}
