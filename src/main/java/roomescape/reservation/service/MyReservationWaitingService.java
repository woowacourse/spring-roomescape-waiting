package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.reservation.dto.MyReservationWaitingResponse;
import roomescape.waiting.service.WaitingService;

@Service
public class MyReservationWaitingService {
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MyReservationWaitingService(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<MyReservationWaitingResponse> findMyReservationsWaitings(Long memberId) {
        return Stream.concat(
                        reservationService.findMyReservations(memberId).stream(),
                        waitingService.findMyWaitings(memberId).stream())
                .sorted(Comparator.comparing(myReservationResponse ->
                        LocalDateTime.of(myReservationResponse.date(), myReservationResponse.startAt())))
                .toList();
    }
}
