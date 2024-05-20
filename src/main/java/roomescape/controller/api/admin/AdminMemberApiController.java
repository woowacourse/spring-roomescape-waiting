package roomescape.controller.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.member.Member;
import roomescape.service.dto.response.member.MemberIdAndNameResponses;
import roomescape.service.member.MemberService;

@RestController
public class AdminMemberApiController {

    private final MemberService memberService;

    public AdminMemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/members")
    public ResponseEntity<MemberIdAndNameResponses> getMembers() {
        List<Member> members = memberService.findMembers();
        return ResponseEntity.ok(MemberIdAndNameResponses.from(members));
    }
}
