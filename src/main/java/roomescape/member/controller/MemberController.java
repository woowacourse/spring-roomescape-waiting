package roomescape.member.controller;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.registration.dto.RegistrationInfo;
import roomescape.registration.domain.reservation.service.ReservationService;
import roomescape.registration.domain.waiting.service.WaitingService;

// todo : 멤버별 예약은 멤버에 있는게 맞을까 예약에 있는게 맞을까
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

    // todo: 정렬 어떻게 할지 생각
    @GetMapping("/member/registrations")
    public List<RegistrationInfo> memberReservationList(@LoginMemberId long memberId) {
        List<RegistrationInfo> reservationsOfMember = reservationService.findMemberReservations(memberId)
                .stream()
                .map((RegistrationInfo::from))
                .toList();

        List<RegistrationInfo> waitingsOfMember = waitingService.findMemberWaitingWithRank(memberId)
                .stream()
                .map((RegistrationInfo::from))
                .toList();

        return Stream.concat(reservationsOfMember.stream(), waitingsOfMember.stream())
                .toList();
    }
}
