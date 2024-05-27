package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminWaitingController {

    private final ReservationService reservationService;

    public AdminWaitingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/waitings/{id}")
    public ReservationResponse approveWaiting(@PathVariable Long id) {
        return reservationService.approveWaiting(id);
    }

    @GetMapping("/waitings")
    public List<WaitingResponse> readWaitings() {
        return reservationService.readWaitings();
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> rejectWaiting(@PathVariable Long id) {
        reservationService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
