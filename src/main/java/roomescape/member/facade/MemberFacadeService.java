package roomescape.member.facade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.service.ReservationService;
import roomescape.waiting.domain.Waiting;

@Service
public class MemberFacadeService {

    private final MemberService memberService;
    private final ReservationService reservationService;

    public MemberFacadeService(MemberService memberService, ReservationService reservationService) {
        this.memberService = memberService;
        this.reservationService = reservationService;
    }

    public List<MemberResponse> findMemberIds() {
        List<Member> members = memberService.findMembersId();
        return members.stream()
                .map(member -> new MemberResponse(member.getId()))
                .toList();
    }

    public List<MemberReservationResponse> findMemberReservations(long memberId) {
        List<Reservation> reservations = reservationService.findStatusReservations(memberId,
                ReservationStatus.RESERVED);
        List<Waiting> waitings = reservationService.findWaitingWithRank(memberId);

        List<MemberReservationResponse> memberReservationRespons = makeResponse(
                reservations, waitings);

        return memberReservationRespons.stream()
                .sorted(Comparator.comparing(MemberReservationResponse::date))
                .toList();
    }

    private List<MemberReservationResponse> makeResponse(List<Reservation> reservations,
                                                         List<Waiting> waitingRanks) {
        List<MemberReservationResponse> memberReservationResponses = new ArrayList<>();

        reservations.forEach(reservation -> memberReservationResponses
                .add(MemberReservationResponse.from(reservation)));

        waitingRanks.forEach(waiting -> memberReservationResponses
                .add(MemberReservationResponse.from(waiting)));

        return memberReservationResponses;
    }
}
