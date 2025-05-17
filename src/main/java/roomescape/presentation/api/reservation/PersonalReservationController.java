package roomescape.presentation.api.reservation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.presentation.api.reservation.response.ReservationWithStatusResponse;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
public class PersonalReservationController {

    private final ReservationService reservationService;

    public PersonalReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findMineReservations(@AuthPrincipal AuthInfo authInfo) {
        List<ReservationWithStatusResult> reservationsWithStatus = reservationService.findReservationsWithStatus(
                authInfo.memberId()
        );
        List<ReservationWithStatusResponse> reservationWithStatusResponses = reservationsWithStatus.stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        return ResponseEntity.ok(reservationWithStatusResponses);
    }
}
