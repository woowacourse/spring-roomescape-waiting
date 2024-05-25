package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationLookupService;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationWaitingResponse;
import roomescape.presentation.auth.LoginMemberId;
import roomescape.presentation.auth.PermissionRequired;

@RestController
public class ReservationWaitingController {
    private final ReservationService reservationService;
    private final ReservationLookupService reservationLookupService;

    public ReservationWaitingController(ReservationService reservationService,
                                        ReservationLookupService reservationLookupService) {
        this.reservationService = reservationService;
        this.reservationLookupService = reservationLookupService;
    }

    @PostMapping("/reservations/queue")
    public ResponseEntity<ReservationWaitingResponse> enqueueWaiting(@LoginMemberId long memberId,
                                                                     @RequestBody @Valid ReservationRequest request) {
        ReservationWaitingResponse response = reservationService.enqueueWaitingList(request.withMemberId(memberId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/queue/{id}")
    public ResponseEntity<Void> cancelWaiting(@LoginMemberId long memberId,
                                              @PathVariable long id) {
        reservationService.cancelWaitingList(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/queue")
    @PermissionRequired
    public ResponseEntity<List<ReservationResponse>> getWaitingList() {
        List<ReservationResponse> responses = reservationLookupService.findAllWaitingReservations();
        return ResponseEntity.ok(responses);
    }
}
