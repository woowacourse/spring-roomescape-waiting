package roomescape.member.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.presentation.dto.MemberRequest;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.application.MemberApplicationService;

@RestController
public class MemberController {

    private final MemberApplicationService memberApplicationService;

    public MemberController(MemberApplicationService memberApplicationService) {
        this.memberApplicationService = memberApplicationService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAll() {
        return ResponseEntity.ok(memberApplicationService.findAll());
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) {
        return ResponseEntity.ok().body(memberApplicationService.save(request));
    }
}
