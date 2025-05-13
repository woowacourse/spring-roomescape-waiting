package roomescape.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.dto.MemberRegisterRequest;
import roomescape.dto.MemberRegisterResponse;
import roomescape.dto.MemberResponse;
import roomescape.service.MemberService;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("signup")
    public String signUpPage() {
        return "signup";
    }

    @PostMapping("/members")
    public ResponseEntity<MemberRegisterResponse> registerMember(@RequestBody final MemberRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.addMember(request));
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getAllMembers());
    }
}
