package roomescape.controller.member;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.member.dto.MemberLoginResponse;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.domain.Member;
import roomescape.service.MemberService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberLoginResponse> getMembers() {
        final List<Member> members = memberService.findAll();
        return members.stream()
                .map(MemberLoginResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<MemberLoginResponse> createMember(@RequestBody @Valid final SignupRequest request) {
        final Member member = memberService.save(request);

        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(member.getId())
                .toUri();
        return ResponseEntity.created(uri)
                .body(MemberLoginResponse.from(member));
    }
}
