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
import roomescape.auth.aop.RequiredRoles;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.common.uri.UriFactory;
import roomescape.reservation.application.ReservationFacade;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.ui.dto.CreateReservationWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.NORMAL)
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id,
                                       @SignInUser final UserSession userSession) {
        reservationFacade.delete(
                ReservationId.from(id), userSession);
        return ResponseEntity.noContent().build();
    }
}
