package roomescape.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.service.dto.request.SignupRequest;
import roomescape.service.dto.response.MemberIdAndNameResponse;
import roomescape.service.member.MemberService;

import java.net.URI;
import java.util.List;

@RestController
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberIdAndNameResponse>> getMembers() {
        List<Member> members = memberService.findMembers();
        return ResponseEntity.ok(
                members.stream()
                        .map(member -> new MemberIdAndNameResponse(member.getId(), member.getName()))
                        .toList()
        );
    }

    @PostMapping("/members")
    public ResponseEntity<MemberIdAndNameResponse> signup(@RequestBody @Valid SignupRequest request) {
        Member member = memberService.signUp(request);
        return ResponseEntity.created(URI.create("/members/" + member.getId()))
                .body(new MemberIdAndNameResponse(member.getId(), member.getName()));
    }
}
