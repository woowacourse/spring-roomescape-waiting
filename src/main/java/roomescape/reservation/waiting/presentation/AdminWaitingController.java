package roomescape.reservation.waiting.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.waiting.application.WaitingReservationFacadeService;

@RestController
@RequestMapping("/admin")
public class AdminWaitingController {

    private final WaitingReservationFacadeService waitingReservationFacadeService;

    public AdminWaitingController(WaitingReservationFacadeService waitingReservationFacadeService) {
        this.waitingReservationFacadeService = waitingReservationFacadeService;
    }

    @GetMapping("/waiting-reservations")
    public ResponseEntity<List<ReservationResponse>> getWaitingReservations() {
        List<ReservationResponse> responses = waitingReservationFacadeService.getWaitingReservations();

        return ResponseEntity.ok().body(responses);
    }

    @PostMapping("/waiting-reservations/{id}")
    public ResponseEntity<Void> acceptWaitingReservation(@PathVariable("id") Long waitingId) {
        waitingReservationFacadeService.acceptWaiting(waitingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/waiting-reservations/{id}")
    public ResponseEntity<Void> denyWaitingReservation(@PathVariable("id") Long waitingId) {
        waitingReservationFacadeService.denyWaiting(waitingId);
        return ResponseEntity.ok().build();
    }
}
