package roomescape.reservation.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.session.Session;
import roomescape.auth.session.annotation.UserSession;
import roomescape.common.uri.UriFactory;
import roomescape.reservation.application.ReservationFacade;
import roomescape.reservation.application.dto.SimpleWaitingReservationResponse;
import roomescape.reservation.ui.dto.CreateReservationWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.WaitingReservationResponse;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(WaitingReservationController.BASE_PATH)
public class WaitingReservationController {

    public static final String BASE_PATH = "/reservations/waiting";

    private final ReservationFacade reservationFacade;

    @GetMapping
    public ResponseEntity<List<WaitingReservationResponse>> getAll() {
        final List<WaitingReservationResponse> reservations = reservationFacade.getAllWaiting();
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<SimpleWaitingReservationResponse> create(
            @RequestBody final CreateReservationWebRequest request,
            @UserSession final Session session
    ) {
        final SimpleWaitingReservationResponse reservationResponse = reservationFacade.addWaiting(
                request.toRequestWithUserId(session.userId())
        );
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationResponse.waitingReservationId()));
        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reservationFacade.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<ReservationResponse> promotion(
            @PathVariable final Long id,
            @RequestBody final CreateReservationWebRequest request,
            @UserSession final Session session
    ) {
        final ReservationResponse reservationResponse = reservationFacade.promotionWaiting(
                id, request.toRequestWithUserId(session.userId())
        );
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationResponse.reservationId()));
        return ResponseEntity.created(location)
                .body(reservationResponse);
    }
}
