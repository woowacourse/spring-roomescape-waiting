package roomescape.member.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.application.MemberService;
import roomescape.member.application.dto.MemberResponse;

@Controller
@RequestMapping("admin/members")
@AllArgsConstructor
public class AdminMemberController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }
}
