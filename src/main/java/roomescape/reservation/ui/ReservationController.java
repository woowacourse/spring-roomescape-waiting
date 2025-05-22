package roomescape.reservation.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.session.Session;
import roomescape.auth.session.annotation.UserSession;
import roomescape.common.uri.UriFactory;
import roomescape.reservation.application.ReservationFacade;
import roomescape.reservation.application.dto.WaitingReservationResponse;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(ReservationController.BASE_PATH)
public class ReservationController {

    public static final String BASE_PATH = "/reservations";
    private static final String WAITING_PATH = BASE_PATH + "/waiting";

    private final ReservationFacade reservationFacade;

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationResponse>> getMine(@UserSession final Session session) {
        final List<ReservationResponse> reservations = reservationFacade.getAllByUserId(session.userId());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/times")
    public ResponseEntity<List<AvailableReservationTimeWebResponse>> getAvailable(
            @RequestParam final LocalDate date,
            @RequestParam final Long themeId) {
        final List<AvailableReservationTimeWebResponse> reservations = reservationFacade.getAvailable(date, themeId);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody final CreateReservationWebRequest request,
            @UserSession final Session session) {
        final ReservationResponse reservationResponse = reservationFacade.create(
                request.toRequestWithUserId(session.userId()));
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationResponse.reservationId()));
        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reservationFacade.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waiting")
    public ResponseEntity<Void> getWaiting() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingReservationResponse> addWaiting(
            @RequestBody final CreateReservationWebRequest request,
            @UserSession final Session session
    ) {
        final WaitingReservationResponse reservationResponse = reservationFacade.addWaiting(
                request.toRequestWithUserId(session.userId())
        );
        final URI location = UriFactory.buildPath(WAITING_PATH, String.valueOf(reservationResponse.waitingReservationId()));
        return ResponseEntity.created(location)
                .body(reservationResponse);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable final Long id) {
        return ResponseEntity.noContent().build();
    }
}
