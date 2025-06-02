package roomescape.presentation.api.reservation;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.WaitingService;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.application.reservation.dto.WaitingWitStatusResult;
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

    @GetMapping("reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findMineReservations(@AuthPrincipal AuthInfo authInfo) {
        List<ReservationWithStatusResult> reservationsWithStatus = reservationService.findReservationsWithStatus(
                authInfo.memberId()
        );
        List<WaitingWitStatusResult> waitingRanks = waitingService.findWaitingRanks(authInfo.memberId());
        return ResponseEntity.ok(createReservationWithStatusResponses(reservationsWithStatus, waitingRanks));
    }

    private List<ReservationWithStatusResponse> createReservationWithStatusResponses(
            List<ReservationWithStatusResult> reservationsWithStatus, List<WaitingWitStatusResult> waitingRanks) {
        List<ReservationWithStatusResponse> reservationWithStatusResponses = reservationsWithStatus.stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        List<ReservationWithStatusResponse> waitingWithStatusResponse = waitingRanks.stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        return Stream.concat(
                reservationWithStatusResponses.stream(),
                waitingWithStatusResponse.stream()
        ).toList();
    }
}
