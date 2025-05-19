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
import roomescape.application.member.command.CreateMemberService;
import roomescape.application.member.query.MemberQueryService;
import roomescape.application.member.query.dto.MemberResult;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final CreateMemberService createMemberService;
    private final MemberQueryService memberQueryService;

    public MemberController(CreateMemberService createMemberService, MemberQueryService memberQueryService) {
        this.createMemberService = createMemberService;
        this.memberQueryService = memberQueryService;
    }

    @PostMapping
    public ResponseEntity<Void> createMember(@Valid @RequestBody SignupRequest signupRequest) {
        createMemberService.register(signupRequest.toRegisterParameter());
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAll() {
        List<MemberResult> memberResults = memberQueryService.findAll();
        List<MemberResponse> memberResponses = memberResults.stream()
                .map(MemberResponse::from)
                .toList();
        return ResponseEntity.ok(memberResponses);
    }
}
