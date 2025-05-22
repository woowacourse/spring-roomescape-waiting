package roomescape.member.ui;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.application.MemberService;
import roomescape.member.application.dto.MemberRequest;
import roomescape.member.application.dto.MemberResponse;

@Controller
@RequestMapping("members")
@AllArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest memberRequest) {
        MemberResponse memberResponse = memberService.add(memberRequest);
        return ResponseEntity.ok(memberResponse);
    }
}
