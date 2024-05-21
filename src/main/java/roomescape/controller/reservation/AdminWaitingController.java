package roomescape.controller.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

import java.util.List;

@RequestMapping("/admin/waitings")
@RestController
public class AdminWaitingController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminWaitingController(final ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservationWaitings() {
        return ResponseEntity.ok(reservationService.findReservationWaitings());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> approveReservationWaiting(@PathVariable final Long id) {
        reservationService.approveReservationWaiting(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable final Long id) {
        waitingService.rejectReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
