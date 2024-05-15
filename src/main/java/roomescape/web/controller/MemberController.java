package roomescape.web.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.member.MemberResponse;
import roomescape.service.MemberService;

@RestController
class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> memberResponses = memberService.getAllMembers();
        return ResponseEntity.ok(memberResponses);
    }
}
