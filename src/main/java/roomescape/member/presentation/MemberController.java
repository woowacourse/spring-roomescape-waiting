package roomescape.member.presentation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.presentation.dto.request.SignupWebRequest;
import roomescape.member.presentation.dto.response.MemberWebResponse;
import roomescape.member.presentation.dto.response.SignUpWebResponse;
import roomescape.member.application.MemberApplicationService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberApplicationService memberApplicationService;

    public MemberController(final MemberApplicationService memberApplicationService) {
        this.memberApplicationService = memberApplicationService;
    }

    @PostMapping
    public ResponseEntity<SignUpWebResponse> signUp(final @RequestBody SignupWebRequest signupWebRequest) {
        SignUpWebResponse response = memberApplicationService.signup(signupWebRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberWebResponse>> findAllRegular() {
        return ResponseEntity.ok(memberApplicationService.findAllRegular());
    }
}
