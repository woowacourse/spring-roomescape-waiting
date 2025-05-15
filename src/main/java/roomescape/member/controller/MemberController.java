package roomescape.member.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.auth.RoleRequired;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @RoleRequired(value = Role.ADMIN)
    @GetMapping("/members")
    public List<MemberInfoResponse> getMembers() {
        return memberService.getAll();
    }
}
