package roomescape.member.presentation.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.service.MemberService;
import roomescape.member.presentation.dto.MemberResponse;

@RestController
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers() {
        final List<MemberResponse> responses = memberService.findMembers()
                .stream()
                .map(MemberResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }
}

