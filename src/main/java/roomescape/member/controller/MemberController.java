package roomescape.member.controller;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.RegistrationInfoDto;
import roomescape.reservation.service.ReservationService;
import roomescape.waiting.service.WaitingService;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MemberController(MemberService memberService, ReservationService reservationService,
                            WaitingService waitingService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/members")
    public List<MemberResponse> memberIdList() {
        return memberService.findMembersId();
    }

    @GetMapping("/member/registrations")
    public List<RegistrationInfoDto> memberReservationList(@LoginMemberId long memberId) {
        List<RegistrationInfoDto> reservationsOfMember = reservationService.findMemberReservations(memberId)
                .stream()
                .map((RegistrationInfoDto::from))
                .toList();

        List<RegistrationInfoDto> waitingsOfMember = waitingService.findMemberWaitingWithRank(memberId)
                .stream()
                .map((RegistrationInfoDto::from))
                .toList();

        return Stream.concat(reservationsOfMember.stream(), waitingsOfMember.stream())
                .toList();
    }
}
