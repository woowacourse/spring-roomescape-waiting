package roomescape.controller.member;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.member.MemberResponseDto;
import roomescape.service.member.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponseDto> getMembers() {
        return memberService.findAll().stream()
                .map((member) -> new MemberResponseDto(member.getId(), member.getName()))
                .toList();
    }
}
