package roomescape.admin.member;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Role;
import roomescape.member.presentation.dto.MemberResponse;

@RestController
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Auth(Role.ADMIN)
    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
    ) {
        return ResponseEntity.ok().body(
                memberService.getMembers()
        );
    }
}
