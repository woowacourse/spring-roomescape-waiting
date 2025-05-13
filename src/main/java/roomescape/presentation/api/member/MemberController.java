package roomescape.presentation.api.member;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.member.MemberService;
import roomescape.application.member.MemberResult;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAll() {
        List<MemberResult> memberResults = memberService.findAll();
        List<MemberResponse> memberResponses = memberResults.stream()
                .map(MemberResponse::from)
                .toList();
        return ResponseEntity.ok(memberResponses);
    }
}
