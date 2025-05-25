package roomescape.application;

import org.springframework.stereotype.Service;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.response.MyReservationResponse;
import roomescape.presentation.dto.response.MyReservationWithWaitingResponse;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MyReservationQueryService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MyReservationQueryService(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<MyReservationWithWaitingResponse> getMyReservationsWithWaitings(LoginMember loginMember) {
        List<MyReservationResponse> myReservations = reservationService.getMyReservations(loginMember);

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
