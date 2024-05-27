package roomescape.member.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.dto.ResourcesResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberSignUpRequest memberSignUpRequest) {
        MemberResponse memberResponse = memberService.save(memberSignUpRequest);

        return ResponseEntity.created(URI.create("/members/" + memberResponse.id())).build();
    }

    @GetMapping("/members")
    public ResponseEntity<ResourcesResponse<MemberResponse>> findAll() {
        List<MemberResponse> members = memberService.findAll();
        ResourcesResponse<MemberResponse> response = new ResourcesResponse<>(members);

        return ResponseEntity.ok(response);
    }
}
