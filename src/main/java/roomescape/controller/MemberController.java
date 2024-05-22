package roomescape.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMemberId;
import roomescape.service.member.MemberService;
import roomescape.service.member.dto.MemberReservationResponse;
import roomescape.service.member.dto.MemberResponse;
import roomescape.service.reservation.ReservationService;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final ReservationService reservationService;

    public MemberController(MemberService memberService, ReservationService reservationService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<MemberResponse> findAllMembers() {
        return memberService.findAll();
    }

    @GetMapping("/reservations")
    public List<MemberReservationResponse> findReservations(@LoginMemberId long memberId) {
        return reservationService.findReservationsOf(memberId);
    }
}
