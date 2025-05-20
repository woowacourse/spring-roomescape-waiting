package roomescape.reservation.reservation.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.common.uri.UriFactory;
import roomescape.reservation.reservation.application.ReservationFacade;
import roomescape.reservation.reservation.ui.dto.CreateReservationWebRequest;
import roomescape.reservation.reservation.application.dto.ReservationResponse;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(ReservationController.BASE_PATH)
public class ReservationController {

    public static final String BASE_PATH = "/reservations";

    private final ReservationFacade reservationFacade;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMine(@SignInUser final UserSession userSession) {
        final List<ReservationResponse> reservations = reservationFacade.getAllByUserId(userSession.id());
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody final CreateReservationWebRequest request,
            @SignInUser final UserSession userSession) {
        final ReservationResponse reservationResponse =
                reservationFacade.create(
                        request.toRequestWithUserId(userSession.id()),
                        userSession);
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationResponse.reservationId()));
        return ResponseEntity.created(location)
                .body(reservationResponse);
    }
}
