package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.SignUpRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.member.MemberSignupResponse;
import roomescape.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers() {
        List<MemberResponse> allMembers = memberService.findAllMembers();
        return ResponseEntity.ok(allMembers);
    }

    @PostMapping("/members")
    public ResponseEntity<MemberSignupResponse> signup(@RequestBody SignUpRequest requestDto) {
        MemberSignupResponse memberSignupResponseDto = memberService.registerMember(requestDto);
        return ResponseEntity.ok().body(memberSignupResponseDto);
    }
}
