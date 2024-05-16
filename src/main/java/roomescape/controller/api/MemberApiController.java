package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.service.dto.response.MemberIdAndNameResponse;
import roomescape.service.member.MemberService;

@RestController
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberIdAndNameResponse>> getMembers() {
        List<Member> members = memberService.findMembers();
        return ResponseEntity.ok(
                members.stream()
                        .map(member -> new MemberIdAndNameResponse(member.getId(), member.getName()))
                        .toList()
        );
    }
}
