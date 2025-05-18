package roomescape.presentation.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.MemberService;
import roomescape.presentation.dto.request.MemberCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers() {
        return ResponseEntity.ok(memberService.getMembers());
    }

    @PostMapping("/members")
    public ResponseEntity<Void> createMember(@RequestBody @Valid MemberCreateRequest request) {
        memberService.createMember(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
