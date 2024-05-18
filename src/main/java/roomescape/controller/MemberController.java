package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.MemberService;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.MemberResponse;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<ListResponse<MemberResponse>> findMembers() {
        ListResponse<MemberResponse> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }
}
