package roomescape.controller.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.ReservationService;

import java.util.List;

@RequestMapping("/admin/waitings")
@RestController
public class AdminWaitingController {

    private final ReservationService reservationService;

    public AdminWaitingController(final ReservationService reservationService) {
        this.reservationService = reservationService;
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
}
