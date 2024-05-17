package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.dto.MemberListResponse;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @GetMapping("/members")
    public ResponseEntity<MemberListResponse> findAllMember() {
        MemberListResponse response = memberService.findAllMember();
        return ResponseEntity.ok().body(response);
    }
}
