package roomescape.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SaveMemberRequest;
import roomescape.member.model.Member;
import roomescape.member.service.MemberService;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public List<MemberResponse> getMembers() {
        return memberService.getMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @PostMapping("/members")
    public MemberResponse saveMember(@RequestBody final SaveMemberRequest request) {
        final Member savedMember = memberService.saveMember(request);
        return MemberResponse.from(savedMember);
    }
}
