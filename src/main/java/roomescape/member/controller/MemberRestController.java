package roomescape.member.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;

@RequestMapping("/members")
@RequiredArgsConstructor
@RestController
public class MemberRestController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers() {
        final List<Member> members = memberService.findAll();
        final List<MemberResponse> memberResponses = members.stream()
                .map(MemberResponse::from)
                .toList();

        return ResponseEntity.ok(memberResponses);
    }
}
