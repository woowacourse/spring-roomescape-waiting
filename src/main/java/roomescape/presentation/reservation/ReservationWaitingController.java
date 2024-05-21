package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationWaitingService;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationWaitingResponse;
import roomescape.presentation.auth.LoginMemberId;

@RestController
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping("/reservations/queue")
    public ResponseEntity<ReservationWaitingResponse> enqueueWaiting(@LoginMemberId long memberId,
                                                                     @RequestBody @Valid ReservationRequest request) {
        ReservationWaitingResponse response = reservationWaitingService.enqueueWaitingList(
                request.withMemberId(memberId)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/queue/{id}")
    public ResponseEntity<Void> cancelWaiting(@LoginMemberId long memberId,
                                              @PathVariable long id) {
        reservationWaitingService.cancelWaitingList(memberId, id);
        return ResponseEntity.noContent().build();
    }
}
