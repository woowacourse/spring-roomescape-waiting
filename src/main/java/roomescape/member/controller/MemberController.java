package roomescape.member.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.service.MemberService;
import roomescape.member.dto.SignupRequest;
import roomescape.member.dto.MemberResponse;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> signup(@RequestBody @Valid final SignupRequest request) {
        final MemberResponse response = memberService.createMember(request);
        return ResponseEntity.created(URI.create("/members" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> members() {
        List<MemberResponse> responses = memberService.findAll();
        return ResponseEntity.ok().body(responses);
    }
}
