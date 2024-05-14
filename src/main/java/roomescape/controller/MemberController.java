package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.MemberResponse;
import roomescape.service.MemberService;
import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAllMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }
}
