package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.service.dto.response.member.MemberIdAndNameResponses;
import roomescape.service.member.MemberService;

@RestController
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<MemberIdAndNameResponses> getMembers() {
        List<Member> members = memberService.findMembers();
        return ResponseEntity.ok(MemberIdAndNameResponses.from(members));
    }
}
