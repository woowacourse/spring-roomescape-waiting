package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.dto.MemberResponse;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAllMember() {
        List<MemberResponse> response = memberService.findAllMember();
        return ResponseEntity.ok().body(response);
    }
}
