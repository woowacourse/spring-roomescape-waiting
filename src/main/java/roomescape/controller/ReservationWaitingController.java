package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.ReservationWaitingService;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @RequestBody @Valid ReservationRequest waitingRequest,
            @LoginMemberId long memberId
    ) {
        ReservationWaitingResponse response = reservationWaitingService.create(waitingRequest, memberId);
        return ResponseEntity.created(URI.create("/reservations/waiting/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(
            @PathVariable("id") long waitingId,
            @LoginMemberId long memberId
    ) {
        reservationWaitingService.deleteById(waitingId, memberId);
        return ResponseEntity.noContent().build();
    }
}
