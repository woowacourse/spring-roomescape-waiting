package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.SignupRequest;
import roomescape.controller.dto.response.ApiResponses;
import roomescape.service.MemberService;
import roomescape.service.dto.response.MemberResponse;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> signup(@RequestBody @Valid SignupRequest request) {
        MemberResponse memberResponse = memberService.createMember(request);
        return ResponseEntity.created(URI.create("/members/" + memberResponse.id()))
                .body(memberResponse);
    }

    @GetMapping
    public ApiResponses<MemberResponse> getAllMembers() {
        List<MemberResponse> memberResponses = memberService.getAllMembers();
        return new ApiResponses<>(memberResponses);
    }
}
