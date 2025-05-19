package roomescape.member.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.member.dto.response.MemberNameSelectResponse;
import roomescape.member.service.MemberServiceFacade;

@RestController
public class MemberController {

    private final MemberServiceFacade memberService;

    @Autowired
    public MemberController(MemberServiceFacade memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberNameSelectResponse>> getMembers() {
        List<MemberNameSelectResponse> memberNames = memberService.findMemberNames();
        return ResponseEntity.ok(memberNames);
    }

    @PostMapping("/members")
    public ResponseEntity<MemberSignUpResponse> signup(@RequestBody @Valid MemberSignUpRequest request) {
        MemberSignUpResponse response = memberService.signup(request);
        return ResponseEntity.ok(response);
    }
}
