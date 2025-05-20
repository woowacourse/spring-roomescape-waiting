package roomescape.member.presentation;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.request.MemberRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> getMembers() {
        return memberService.findAllMembers();
    }

    @PostMapping
    public ResponseEntity<MemberResponse> signUp(@RequestBody @Valid MemberRequest memberRequest) {
        return ResponseEntity.ok(memberService.createMember(memberRequest));
    }
}
