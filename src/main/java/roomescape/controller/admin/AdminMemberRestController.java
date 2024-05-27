package roomescape.controller.admin;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.MemberService;
import roomescape.service.dto.member.MemberResponse;

@RestController
public class AdminMemberRestController {

    private final MemberService memberService;

    public AdminMemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/members")
    public List<MemberResponse> findAllMembers() {
        return memberService.findAllMembers();
    }
}
