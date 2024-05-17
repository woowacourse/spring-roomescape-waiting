package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.FindMemberNameResponse;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<FindMemberNameResponse>> findAll() {
        List<FindMemberNameResponse> response = memberService.findAll()
            .stream()
            .map(FindMemberNameResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}
