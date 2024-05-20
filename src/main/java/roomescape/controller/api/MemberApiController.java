package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.api.dto.response.MembersResponse;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberApiController {
    private final MemberService memberService;

    public MemberApiController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<MembersResponse> getAllMembers() {
        final var output = memberService.getAllMembers();
        return ResponseEntity.ok(MembersResponse.toResponse(output));
    }
}
