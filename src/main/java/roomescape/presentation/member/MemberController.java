package roomescape.presentation.member;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.member.MemberService;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.member.dto.response.MemberResponse;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> register(@RequestBody @Valid MemberRegisterRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAll() {
        List<MemberResponse> responses = memberService.findAll();
        return ResponseEntity.ok(responses);
    }
}
