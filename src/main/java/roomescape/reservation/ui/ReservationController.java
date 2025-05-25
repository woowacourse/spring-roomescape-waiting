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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.aop.RequiredRoles;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.reservation.application.ReservationFacade;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.SlotSequenceResponse;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.ui.dto.CreateReservationWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.NORMAL)
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @GetMapping
    public ResponseEntity<List<SlotSequenceResponse>> getMine(@SignInUser final UserSession userSession) {
        final List<SlotSequenceResponse> response = reservationFacade.getAllSlotSequenceByUserId(userSession.id());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody final CreateReservationWebRequest request,
            @SignInUser final UserSession userSession) {
        final ReservationResponse response =
                reservationFacade.create(
                        request.toRequestWithUserId(userSession.id()));

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
