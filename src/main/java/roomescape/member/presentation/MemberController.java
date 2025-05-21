package roomescape.member.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.presentation.dto.MemberRequest;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.application.MemberFacadeService;

@RestController
public class MemberController {

    private final MemberFacadeService memberFacadeService;

    public MemberController(MemberFacadeService memberFacadeService) {
        this.memberFacadeService = memberFacadeService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAll() {
        return ResponseEntity.ok(memberFacadeService.findAll());
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) {
        return ResponseEntity.ok().body(memberFacadeService.save(request));
    }
}
