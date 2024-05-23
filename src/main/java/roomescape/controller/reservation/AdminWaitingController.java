package roomescape.controller.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.WaitingService;

import java.util.List;

@RequestMapping("/admin/waitings")
@RestController
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservationWaitings() {
        return ResponseEntity.ok(waitingService.findReservationWaitings());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> approveReservationWaiting(@PathVariable final Long id) {
        waitingService.approveReservationWaiting(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable final Long id) {
        waitingService.rejectReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
