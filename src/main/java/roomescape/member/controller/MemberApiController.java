package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.service.MemberService;

@Controller
@RequestMapping("/members")
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAll() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @PostMapping
    public ResponseEntity<MemberResponse> add(@RequestBody MemberSignupRequest memberSignupRequest) {
        MemberResponse memberResponse = memberService.add(memberSignupRequest);
        return ResponseEntity.ok(memberResponse);
    }
}
