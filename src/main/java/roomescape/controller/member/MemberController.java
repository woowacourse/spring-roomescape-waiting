package roomescape.controller.member;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMemberId;
import roomescape.service.member.MemberService;
import roomescape.service.member.dto.MemberReservationResponse;
import roomescape.service.member.dto.MemberResponse;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> findAllMembers() {
        return memberService.findAll();
    }

    @GetMapping("/reservations")
    public List<MemberReservationResponse> findReservations(@LoginMemberId long memberId) {
        return memberService.findReservations(memberId);
    }
}
