package roomescape.controller.member;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.member.dto.MemberLoginResponse;
import roomescape.domain.Member;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberLoginResponse> getMembers() {
        final List<Member> members = memberService.findAll();
        return members.stream()
                .map(MemberLoginResponse::from)
                .toList();
    }
}
