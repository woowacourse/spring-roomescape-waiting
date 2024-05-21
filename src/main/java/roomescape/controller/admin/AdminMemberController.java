package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.MemberResponses;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/admin/members")
public class AdminMemberController {
    private final MemberService memberService;

    public AdminMemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    ResponseEntity<MemberResponses> read() {
        final MemberResponses memberResponses = memberService.findEntireMembers();
        return ResponseEntity.ok(memberResponses);
    }
}
