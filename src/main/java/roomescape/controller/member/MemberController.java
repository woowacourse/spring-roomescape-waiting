package roomescape.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.RoleAllowed;
import roomescape.domain.member.MemberRole;
import roomescape.service.member.MemberService;
import roomescape.service.member.dto.MemberListResponse;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @RoleAllowed(MemberRole.ADMIN)
    @GetMapping("/members")
    public ResponseEntity<MemberListResponse> findAllMember() {
        MemberListResponse response = memberService.findAllMember();
        return ResponseEntity.ok().body(response);
    }
}
