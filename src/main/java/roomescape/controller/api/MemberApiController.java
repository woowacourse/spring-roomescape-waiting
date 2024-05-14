package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.controller.api.dto.response.MembersResponse;
import roomescape.service.MemberService;
import roomescape.service.dto.output.MemberCreateOutput;

import java.net.URI;

@RestController
@RequestMapping("/members")
public class MemberApiController {
    private final MemberService memberService;

    public MemberApiController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> createMember(@RequestBody final MemberCreateRequest request) {
        final MemberCreateOutput output = memberService.createMember(request.toInput());
        return ResponseEntity.created(URI.create("/reservations/" + output.id()))
                .build();
    }
    @GetMapping
    public ResponseEntity<MembersResponse> getAllMembers(){
        final var output = memberService.getAllMembers();
        return ResponseEntity.ok(MembersResponse.toResponse(output));
    }
}
