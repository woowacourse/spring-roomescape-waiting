package roomescape.member.controller;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.registration.dto.RegistrationInfo;
import roomescape.registration.reservation.service.ReservationService;
import roomescape.registration.waiting.service.WaitingService;

// todo : 멤버별 예약은 멤버에 있는게 맞을까 예약에 있는게 맞을까...흠
// 예약 컨트롤러에 있는게 맞는것 같은데 그럼 RegistrationController만든담에 reservationService, waitingService 필드로 갖게 해야됨.
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

    // todo: 정렬을 등록 순으로 하려면... 큰일낫넹 / 정렬 기준 내 맘대로 할까
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
