package roomescape.member.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.member.facade.MemberFacadeService;

@RestController
public class MemberController {

    private final MemberFacadeService memberFacadeService;

    public MemberController(MemberFacadeService memberFacadeService) {
        this.memberFacadeService = memberFacadeService;
    }

    @GetMapping("/members")
    public List<MemberResponse> memberIdList() {
        return memberFacadeService.findMemberIds();
    }

    @GetMapping("/members/reservations")
    public List<MemberReservationResponse> memberReservationList(@LoginMemberId long id) {
        return memberFacadeService.findMemberReservations(id);
    }
}
