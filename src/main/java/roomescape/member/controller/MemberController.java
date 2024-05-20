package roomescape.member.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;

    public MemberController(MemberService memberService, ReservationService reservationService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
    }

    @GetMapping("/members")
    public List<MemberResponse> memberIdList() {
        return memberService.findMembersId();
    }

    @GetMapping("/member/reservations")
    public List<ReservationWaitingResponse> memberReservationList(@LoginMemberId long id) {
        return reservationService.findMemberReservations(id);
    }
}
