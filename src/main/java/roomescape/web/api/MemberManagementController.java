package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.MemberService;
import roomescape.service.dto.request.member.SignupRequest;
import roomescape.service.dto.response.member.MemberResponse;

@RestController
@RequiredArgsConstructor
public class MemberManagementController {
    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        long createdId = memberService.signup(request);
        return ResponseEntity.created(URI.create("/members/" + createdId)).build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAllMembers() {
        List<MemberResponse> allMember = memberService.findAllMember();
        return ResponseEntity.ok(allMember);
    }

    @DeleteMapping("/members/{idMember}")
    public ResponseEntity<Void> withdrawal(@PathVariable("idMember") Long memberId) {
        memberService.withdrawal(memberId);
        return ResponseEntity.noContent().build();
    }
}
