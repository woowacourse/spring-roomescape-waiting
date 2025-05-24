package roomescape.member.presentation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.service.MemberService;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignupRequest;

@RestController
@RequestMapping("members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMember() {
        return ResponseEntity.ok().body(memberService.findAllMembers());
    }

    @PostMapping
    public ResponseEntity<MemberResponse> addMember(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.created(URI.create("/members")).body(memberService.addMember(signupRequest));
    }
}
