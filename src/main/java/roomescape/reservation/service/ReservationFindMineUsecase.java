package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.waiting.service.WaitingService;

@Component
public class ReservationFindMineUsecase {
    private static final Comparator<MyReservationResponse> RESERVATION_SORTING_COMPARATOR = Comparator
            .comparing(MyReservationResponse::date).thenComparing(MyReservationResponse::startAt);

    private final ReservationFindService reservationFindService;
    private final WaitingService waitingService;

    public ReservationFindMineUsecase(ReservationFindService reservationFindService, WaitingService waitingService) {
        this.reservationFindService = reservationFindService;
        this.waitingService = waitingService;
    }

    public List<MyReservationResponse> execute(Long memberId) {
        List<MyReservationResponse> reservations = reservationFindService.findReservationsByMemberId(memberId);
        List<MyReservationResponse> waitings = waitingService.findWaitingsByMemberId(memberId);

        return makeMyReservations(reservations, waitings);
    }

    private List<MyReservationResponse> makeMyReservations(List<MyReservationResponse> reservations,
                                                           List<MyReservationResponse> waitings) {
        List<MyReservationResponse> response = new ArrayList<>();
        response.addAll(reservations);
        response.addAll(waitings);
        response.sort(RESERVATION_SORTING_COMPARATOR);
        return response;
    }
}
