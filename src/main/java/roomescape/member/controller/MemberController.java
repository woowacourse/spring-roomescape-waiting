package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers(Member member) {
        member.validateAdminOrThrow();
        List<MemberResponse> response = memberService.findAll()
                .stream()
                .map(value -> new MemberResponse(value.getId(), value.getName()))
                .toList();
        return ResponseEntity.ok().body(response);
    }
}
