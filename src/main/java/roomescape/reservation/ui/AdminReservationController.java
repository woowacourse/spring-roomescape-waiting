package roomescape.reservation.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.aop.RequiredRoles;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.reservation.application.ReservationFacade;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.ADMIN)
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationFacade reservationFacade;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        final List<ReservationResponse> reservations = reservationFacade.getAll();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> searchReservations(
            @ModelAttribute final ReservationSearchWebRequest request) {
        return ResponseEntity.ok(
                reservationFacade.getByParams(request.toServiceRequest()));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody final CreateReservationWithUserIdWebRequest request,
            @SignInUser final UserSession userSession) {
        final ReservationResponse response = reservationFacade.create(request.toServiceRequest(), userSession);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.reservationId())
                .toUri();

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id,
                                       @SignInUser final UserSession userSession) {
        reservationFacade.delete(
                ReservationId.from(id), userSession);
        return ResponseEntity.noContent().build();
    }
}
