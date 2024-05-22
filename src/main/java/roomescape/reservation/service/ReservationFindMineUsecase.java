package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.waiting.service.WaitingService;

public class ReservationFindMineUsecase {
    private static final Comparator<MyReservationResponse> RESERVATION_SORTING_COMPARATOR = Comparator.comparing(
            MyReservationResponse::date).thenComparing(MyReservationResponse::startAt);

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationFindMineUsecase(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        List<MyReservationResponse> reservations = reservationService.findReservationsByMemberId(memberId);
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
