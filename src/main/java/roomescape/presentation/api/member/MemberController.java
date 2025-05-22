package roomescape.presentation.api.member;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.member.MemberService;
import roomescape.application.member.dto.MemberResult;

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

    @PostMapping
    public ResponseEntity<Void> createMember(@Valid @RequestBody SignupRequest signupRequest) {
        memberService.register(signupRequest.toServiceParam());
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}
