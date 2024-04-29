package roomescape.member.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.member.dto.JoinRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<Member>> readAll() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> createMember(@RequestBody @Valid JoinRequest joinRequest) {
        MemberResponse createdResponse = memberService.joinMember(joinRequest);
        URI createdUri = URI.create("/members/" + createdResponse.id());
        return ResponseEntity.created(createdUri).body(createdResponse);
    }
}
