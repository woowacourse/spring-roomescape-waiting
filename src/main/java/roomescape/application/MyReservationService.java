package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.response.MyReservationResponse;
import roomescape.presentation.dto.response.MyReservationWithWaitingResponse;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class MyReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingService waitingService;
    private final MemberService memberService;

    public MyReservationService(ReservationRepository reservationRepository, WaitingService waitingService, MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.waitingService = waitingService;
        this.memberService = memberService;
    }

    public List<MyReservationResponse> getMyReservations(LoginMember loginMember) {
        Member member = memberService.findMemberById(loginMember.id());
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return MyReservationResponse.from(reservations);
    }

    public List<MyReservationWithWaitingResponse> getMyReservationsWithWaitings(LoginMember loginMember) {
        List<MyReservationResponse> myReservations = getMyReservations(loginMember);

        List<WaitingWithRank> myWaitingsWithRank = waitingService.getMyWaitingsWithRank(loginMember);

        List<MyReservationWithWaitingResponse> reservationDtos = myReservations.stream()
                .map(MyReservationWithWaitingResponse::fromReservation)
                .toList();

        List<MyReservationWithWaitingResponse> waitingDtos = myWaitingsWithRank.stream()
                .map(MyReservationWithWaitingResponse::fromWaiting)
                .toList();

        return Stream.concat(reservationDtos.stream(), waitingDtos.stream())
                .sorted(Comparator.comparing(MyReservationWithWaitingResponse::date))
                .toList();
    }
}
