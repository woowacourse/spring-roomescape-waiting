package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.MemberService;
import roomescape.dto.MemberResponse;
import roomescape.dto.MemberSignUpRequest;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> members() {
        List<MemberResponse> memberResponses = memberService.findAll();
        return ResponseEntity.ok(memberResponses);
    }

    @PostMapping
    public ResponseEntity<MemberResponse> save(@RequestBody @Valid MemberSignUpRequest memberSignUpRequest) {
        MemberResponse memberResponse = memberService.save(memberSignUpRequest);
        return ResponseEntity.created(URI.create("/members/" + memberResponse.id()))
                .body(memberResponse);
    }
}
