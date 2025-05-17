package roomescape.member.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.member.application.service.MemberService;
import roomescape.member.domain.Role;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.dto.SignUpResponse;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Auth(Role.GUEST)
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(
            @RequestBody @Valid SignUpRequest signUpRequest
    ) {
        return ResponseEntity.ok().body(
                memberService.signUp(signUpRequest)
        );
    }

    @Auth(Role.ADMIN)
    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
    ) {
        return ResponseEntity.ok().body(
                memberService.getMembers()
        );
    }
}
