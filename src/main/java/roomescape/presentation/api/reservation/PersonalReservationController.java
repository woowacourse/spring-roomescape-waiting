package roomescape.presentation.api.reservation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.WaitingService;
import roomescape.presentation.api.reservation.response.ReservationWithStatusResponse;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
public class PersonalReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public PersonalReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findMineReservations(@AuthPrincipal AuthInfo authInfo) {
        Long memberId = authInfo.memberId();
        List<ReservationWithStatusResponse> confirmedReservations = reservationService.findReservationsWithStatus(
                        memberId
                )
                .stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        List<ReservationWithStatusResponse> waitingReservations = waitingService.findWaitingByMemberId(memberId)
                .stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        List<ReservationWithStatusResponse> combined = Stream.concat(
                        confirmedReservations.stream(),
                        waitingReservations.stream()
                )
                .sorted(Comparator.comparing(ReservationWithStatusResponse::date))
                .toList();
        return ResponseEntity.ok(combined);
    }
}
