package roomescape.presentation.api.reservation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.query.ReservationQueryService;
import roomescape.application.reservation.query.WaitingQueryService;
import roomescape.presentation.api.reservation.response.ReservationWithStatusResponse;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
public class PersonalReservationController {

    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    public PersonalReservationController(ReservationQueryService reservationQueryService,
                                         WaitingQueryService waitingQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.waitingQueryService = waitingQueryService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStatusResponse>> findMineReservations(@AuthPrincipal AuthInfo authInfo) {
        Long memberId = authInfo.memberId();
        List<ReservationWithStatusResponse> confirmedReservations = reservationQueryService.findReservationsWithStatus(
                        memberId
                )
                .stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
        List<ReservationWithStatusResponse> waitingReservations = waitingQueryService.findWaitingByMemberId(memberId)
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
