package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.dto.request.ReservationWaitingRequest;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.presentation.Auth;
import roomescape.presentation.dto.Accessor;
import roomescape.presentation.dto.request.ReservationWaitingWebRequest;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {

    private final ReservationService reservationService;

    public ReservationWaitingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservationWaiting(
            @RequestBody @Valid ReservationWaitingWebRequest request,
            @Auth Accessor accessor
    ) {
        ReservationWaitingRequest reservationWaitingRequest = request.toReservationWaitingRequest(accessor.id());
        ReservationResponse reservationResponse = reservationService.addReservationWaiting(reservationWaitingRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id() + "/waiting"))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(
            @PathVariable Long id,
            @Auth Accessor accessor
    ) {
        reservationService.deleteReservationWaitingById(id, accessor.id());

        return ResponseEntity.noContent().build();
    }
}
