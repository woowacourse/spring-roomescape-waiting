package roomescape.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.member.dto.MemberResponse;
import roomescape.domain.member.service.MemberService;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMemberList() {
        List<MemberResponse> memberResponse = memberService.findAll()
                .stream()
                .map((MemberResponse::from))
                .toList();
        return ResponseEntity.ok(memberResponse);
    }
}
