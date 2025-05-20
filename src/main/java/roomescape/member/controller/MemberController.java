package roomescape.member.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.response.SignUpResponse;
import roomescape.member.service.MemberApplicationService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberApplicationService memberApplicationService;

    public MemberController(final MemberApplicationService memberApplicationService) {
        this.memberApplicationService = memberApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAllUsers() {
        return ResponseEntity.ok(memberApplicationService.findAllUsers());
    }

    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(final @RequestBody SignupRequest signupRequest) {
        SignUpResponse response = memberApplicationService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
