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
import roomescape.member.service.MemberModuleService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberModuleService memberModuleService;

    public MemberController(final MemberModuleService memberModuleService) {
        this.memberModuleService = memberModuleService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAllUsers() {
        return ResponseEntity.ok(memberModuleService.findAllUsers());
    }

    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(final @RequestBody SignupRequest signupRequest) {
        SignUpResponse response = memberModuleService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
