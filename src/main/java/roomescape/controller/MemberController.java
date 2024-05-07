package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.response.MemberResponse;
import roomescape.model.member.Member;
import roomescape.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers() {
        List<Member> members = memberService.findAllMembers();
        List<MemberResponse> response = members.stream()
                .map(MemberResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
